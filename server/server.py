#!/usr/bin/env python
# encoding: utf-8
import json
from flask import Flask, request, jsonify
from flask_mongoengine import MongoEngine
from enum import Enum
from flask_sock import Sock
import json

app = Flask(__name__)
app.config['MONGODB_SETTINGS'] = {
    'db': 'CMov',
    'host': 'localhost',
    'port': 27017
}
db = MongoEngine()
db.init_app(app)

sock = Sock(app)

websockets = {}  # dict of websockets by username
'''
Enums
'''


class RoomType(Enum):
    PUBLIC = 1,
    PRIVATE = 2,
    GEO = 3,


''''
Data Models
'''


class Chatroom(db.Document):
    name = db.StringField()
    users = db.ListField(db.ReferenceField('User'))
    messages = db.ListField(db.ReferenceField('Message'))
    roomType = db.IntField()
    # latitude and longitude of the chatroom
    latitude = db.FloatField()
    longitude = db.FloatField()
    radius = db.FloatField()

    def to_json(self):
        return {
            'name': self.name,
            'users': [],
            'messages': [],
            'roomType': self.roomType,
            'latitude': str(self.latitude),
            'longitude': str(self.longitude),
            'radius': str(self.radius)
        }


class User(db.Document):
    username = db.StringField()
    password = db.StringField()
    chatrooms = db.ListField(db.ReferenceField('Chatroom'))

    def to_json(self):
        return {"username": self.username, "password": self.password, "chatroom": self.chatrooms}

class Report(db.Document):
    
    complainUser = db.ReferenceField(User)
    reportedUser = db.ReferenceField(User)

    def to_json(self):
        return {"complainUser": self.complainUser, "reportedUser": self.reportedUser}


class Message(db.Document):
    content = db.StringField()
    user = db.ReferenceField(User)
    chatroom = db.ReferenceField(Chatroom)
    reports = db.ListField(db.ReferenceField('Report'))

    def to_json(self):
        return {"content": self.content, "user": self.user, "chatroom": self.chatroom}


'''
Message Structure
{
    "username": "",
    "password": "",
    "chatroom": "",
    "message": ""
    "contentType": ""
}
'''

'''
websockets
'''

# recieve a message from a user and send it to all users in the room


@sock.route('/message')
def message(ws):
    data = ws.receive()
    data = json.loads(data)
    username = data['username']
    password = data['password']
    room = data['room']
    message = data['message']
    contentType = data['contentType']
    # check if user exists
    # get user
    user = User.objects(username=username).first()
    if user is None:
        ws.send(json.dumps({'error': 'User does not exist'}))
        return
    # check if password is correct
    if not checkPassword(username, password):
        ws.send(json.dumps({'error': 'Password is incorrect'}))
        return
    # check if room exists
    if not roomExists(room):
        ws.send(json.dumps({'error': 'Room does not exist'}))
        return
    # check if user is in room
    if user not in Chatroom.objects(name=room).first().users:
        ws.send(json.dumps({'error': 'User is not in room'}))
        return
    # create message
    message = Message(content=message, user=user, chatroom=Chatroom.objects(
        name=room).first(), id=len(Chatroom.objects(name=room).first().messages)).save()
    # append message to room
    Chatroom.objects(name=room).first().messages.append(message).save()
    # send message to all users in room
    for user in Chatroom.objects(name=room).first().users:
        # send message to user by websocket in websockets dict
        websockets[user.username].send(
            json.dumps({'message': message.to_json()}))


# start websocket connection


@sock.route('/connect')
def connect(socket):
    user = User.objects(username=request.form['user']).first()
    print(user, " connected")
    if user is None:
        return jsonify({"status": "error", "message": "user not found"})
    else:
        websockets[user.username] = socket
        socket.send("Connected")
        return jsonify({"status": "ok"})

# close websocket connection


@sock.route('/disconnect')
def disconnect(socket):
    data = request.form['name']
    user = User.objects(username=data['user']).first()
    if user is None:
        return jsonify({"status": "error", "message": "user not found"})
    else:
        del websockets[user.username]
        return jsonify({"status": "ok"})


'''
Rooms
'''

# check if a room already exists


def roomExists(name):
    room = Chatroom.objects(name=name).first()
    if room is None:
        return False
    else:
        return True


# creates a room with a given name


@app.route('/room/create', methods=['POST'])
def createRoom():
    name = request.form['name']
    roomType = request.form['roomType']
    # from string to roomType enum
    roomType = RoomType[roomType.upper()].value[0]
    print(roomType)
    if roomExists(name):
        return jsonify({"status": "error", "message": "Room already exists"})
    # check if roomType is valid
    if roomType not in [1, 2, 3]:
        return jsonify({"status": "error", "message": "Invalid room type"})
    # check if roomType is of type 3
    if roomType == RoomType.GEO.value[0]:
        # get latitude and longitude
        latitude = request.form['latitude']
        longitude = request.form['longitude']
        radius = request.form['radius']
        # create room
        room = Chatroom(name=name, roomType=roomType, latitude=latitude,
                        longitude=longitude, radius=float(radius)).save()
        return jsonify({"status": "success", "message": "Room created"})
    # create room with longitude and latitude of 0
    room = Chatroom(name=name, roomType=roomType, latitude=0,
                    longitude=0, radius=0).save()
    return jsonify({"status": "success", "message": "Room created"})

# user joins a room


@app.route('/room/join', methods=['POST'])
def joinRoom():
    room = request.form['room']
    user = request.form['user']
    # check if room exists
    if not roomExists(room):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    user = User.objects(username=user).first()
    room = Chatroom.objects(name=room).first()
    if user is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user is in room
    if user in room.users:
        return jsonify({"status": "error", "message": "User is already in room"})
    # add user to room
    room.update(push__users=user)
    # add room to user
    print(room.to_json())
    print(user.to_json())
    user.update(push__chatrooms=room)
    return jsonify({"status": "success", "message": "User joined room"})

# user leaves a room


@app.route('/room/leave', methods=['POST'])
def leaveRoom():
    name = request.form['name']
    user = request.form['user']
    # check if room exists
    room = Chatroom.objects(name=name).first()
    if room is None:
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    user = User.objects(username=user).first()
    if user is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user is in room
    if user not in room.users:
        return jsonify({"status": "error", "message": "User is not in room"})
    # remove user from room
    room.users.remove(user)
    room.save()
    # remove room from user
    user.chatrooms.remove(room)
    user.save()
    return jsonify({"status": "success", "message": "User left room"})

# gets all rooms


@app.route('/room/get/all', methods=['GET'])
def get_rooms():
    rooms = Chatroom.objects()
    return jsonify([room.to_json() for room in rooms])

# get all rooms of a user


@app.route('/room/get/user', methods=['POST'])
def get_user_rooms():
    user = request.form['user']
    # check if user exists
    user = User.objects(username=user).first()
    if user is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # get all rooms of user
    rooms = user.chatrooms
    return jsonify([room.to_json() for room in rooms])


# get all rooms of a can join


@app.route('/room/get/join', methods=['POST'])
def get_join_rooms():
    user = request.form['user']
    # check if user exists
    user = User.objects(username=user).first()
    if user is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # get all rooms
    rooms = Chatroom.objects()
    # get all rooms of user
    user_rooms = user.chatrooms
    # get all rooms that user can join
    join_rooms = [room for room in rooms if room not in user_rooms]
    # remove rooms of type 2
    join_rooms = [room for room in join_rooms if room.roomType !=
                  RoomType.PRIVATE.value[0]]
    # return all rooms that user can join
    return jsonify([room.to_json() for room in join_rooms])

# deletes a room


@app.route('/room/delete', methods=['POST'])
def deleteRoom():
    name = request.form['name']
    # check if room exists
    if not roomExists(name):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # remove room from all users
    users = User.objects()
    for user in users:
        # check if user is in room
        if user.chatrooms.filter(name=name).count() > 0:
            room = Chatroom.objects(name=name).first()
            user.chatrooms.remove(room)
            user.save()
    # delete room
    Chatroom.objects(name=name).delete()
    return jsonify({"status": "success", "message": "Room deleted"})

# get id of a room


@app.route('/room/get/id', methods=['POST'])
def get_room_id():
    name = request.form['name']
    room = Chatroom.objects(name=name).first()
    if room is None:
        return jsonify({"status": "error", "message": "Room does not exist"})
    return jsonify({"status": "success", "message": str(room.id)})

# get name of room by id


@app.route('/room/get/name', methods=['POST'])
def get_room_name():
    id = request.form['id']
    room = Chatroom.objects(id=id).first()
    if room is None:
        return jsonify({"status": "error", "message": "Room does not exist"})
    return jsonify({"status": "success", "message": room.name})

# user joins a room by id


@app.route('/room/join/id', methods=['POST'])
def join_room_id():
    id = request.form['id']
    user = request.form['user']
    # check if room exists
    room = Chatroom.objects(id=id).first()
    if room is None:
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    user = User.objects(username=user).first()
    if user is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user is in room
    if user in room.users:
        return jsonify({"status": "error", "message": "User is already in room"})
    # add user to room
    room.update(push__users=user)
    # add room to user
    user.update(push__chatrooms=room)
    return jsonify({"status": "success", "message": "User joined room"})


'''
Users
'''
# creates a user


@app.route('/user/create', methods=['POST'])
def create_user():
    username = request.form["username"]
    password = request.form["password"]
    print(username)
    print(password)
    if User.objects(username=username).first() is not None:
        return jsonify({"status": "error", "message": "User already exists"})
    User(username=username,
         password=password).save()
    return jsonify({"status": "success", "message": "User created"})

# delete user


@app.route('/user/delete', methods=['DELETE'])
def delete_user():
    username = request.form["username"]
    password = request.form["password"]

    # check if user exists
    if User.objects(username=username).first() is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if password is correct
    if User.objects(username=username).first().password != password:
        return jsonify({"status": "error", "message": "Password is incorrect"})
    # remove user from all rooms
    for room in Chatroom.objects():
        # check if user is in room
        if room.users.filter(username=username).count() > 0:
            room.users.remove(User.objects(username=username).first())
            room.save()
    # delete user
    User.objects(username=username).delete()
    return "User {} deleted".format(username)

# check if password is correct


def checkPassword(username, password):
    user = User.objects(username=username).first()
    if user is None:
        return False
    else:
        return user.password == password

# user login


@app.route('/user/login', methods=['POST'])
def login():
    username = request.form["username"]
    password = request.form["password"]
    # check if user exists
    if User.objects(username=username).first() is None:
        return jsonify({"status": "error", "message": "User or Password is incorrect"})
    # check if password is correct
    if not checkPassword(username, password):
        return jsonify({"status": "error", "message": "User or Password is incorrect"})
    return jsonify({"status": "success", "message": "User logged in"})


'''
Messages
'''

# user sends message to room


def userHasAccess(user, param):
    return True

# get timestamp of message


@app.route('/message/get/timestamp', methods=['POST'])
def get_message_timestamp():
    id = request.form['id']
    message = Message.objects(id=id).first()
    if message is None:
        return jsonify({"status": "error", "message": "Message does not exist"})
    return jsonify({"status": "success", "message": str(message.id.generation_time)})


@app.route('/message/send', methods=['POST'])
def send_message():
    room = request.form['chatroom']
    user = request.form['username']
    message = request.form['message']
    # check if room exists
    if not roomExists(room):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    if User.objects(username=user).first() is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user has access to room
    if not userHasAccess(user, Chatroom.objects(name=room).first()):
        return jsonify({"status": "error", "message": "User does not have access to room"})
    # check if user is in room
    roomObj = Chatroom.objects(name=room).first()
    userObj = User.objects(username=user).first()
    if userObj not in roomObj.users:
        return jsonify({"status": "error", "message": "User not in room"})

    msgObj = Message(user=userObj, content=message)
    msgObj.save()

    roomObj.update(push__messages=msgObj.to_dbref())
    roomObj.save()
    return jsonify({"status": "success", "message": "Message sent"})


@app.route('/message/get', methods=['POST'])
def get_message():
    room = request.form['chatroom']
    user = request.form['username']
    msgID = request.form['msgID']

    # check if room exists
    if not roomExists(room):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    if User.objects(username=user).first() is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user has access to room
    if not userHasAccess(user, Chatroom.objects(name=room).first()):
        return jsonify({"status": "error", "message": "User does not have access to room"})
    # check if user is in room
    roomObj = Chatroom.objects(name=room).first()
    userObj = User.objects(username=user).first()
    if userObj not in roomObj.users:
        return jsonify({"status": "error", "message": "User not in room"})

    if (len(roomObj.messages) - 1) < int(msgID):
        return jsonify({"status": "error", "message": f"Group {room} hasn't message with ID {msgID}"})

    msgObj = roomObj.messages[int(msgID)]
    return jsonify(
        {
            "status": "success",
            "message": "Message - GET",
            "username": msgObj.user.username,
            "message": msgObj.content,
            "timestamp": str(msgObj.id.generation_time)[0:16]
        })

# return the last existing msgID in a groupChat


@app.route('/message/getLastID', methods=['POST'])
def get_last_message_id():
    room = request.form['chatroom']
    user = request.form['username']
    pwd = request.form['password']

    # check if room exists
    if not roomExists(room):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    if User.objects(username=user).first() is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user has access to room
    if not userHasAccess(user, Chatroom.objects(name=room).first()):
        return jsonify({"status": "error", "message": "User does not have access to room"})
    # check if user is in room
    roomObj = Chatroom.objects(name=room).first()
    userObj = User.objects(username=user).first()
    if userObj not in roomObj.users:
        return jsonify({"status": "error", "message": "User not in room"})

    roomObj = Chatroom.objects(name=room).first()

    return jsonify(
        {
            "status": "success",
            "message": str(len(roomObj.messages) - 1)
        })

@app.route('/message/report', methods=['POST'])
def report_message():
    room = request.form['chatroom']
    complainUser = request.form['username']
    password = request.form['password']
    messageID = request.form['messageID']
    # check if room exists
    if not roomExists(room):
        return jsonify({"status": "error", "message": "Room does not exist"})
    # check if user exists
    if User.objects(username=complainUser).first() is None:
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if user has access to room
    if not userHasAccess(complainUser, Chatroom.objects(name=room).first()):
        return jsonify({"status": "error", "message": "User does not have access to room"})
    # check if user is in room
    roomObj = Chatroom.objects(name=room).first()
    userObj = User.objects(username=complainUser).first()
    if userObj not in roomObj.users:
        return jsonify({"status": "error", "message": "User not in room"})

    # get reported msg
    reportedMsg = roomObj.messages[int(messageID)]
    reportedUser = reportedMsg.user
    # build report
    reportObj = Report(complainUser=userObj, reportedUser=message)
    msgObj.save()

    roomObj.update(push__messages=msgObj.to_dbref())
    roomObj.save()
    return jsonify({"status": "success", "message": "Message sent"})

'''
Mainpage / Debug
'''

# ping


@app.route('/ping', methods=['GET'])
def ping():
    return "pong"


@ app.route('/')
def hello_world():
    return "Hello World"


if __name__ == '__main__':
    app.run(debug=True)
