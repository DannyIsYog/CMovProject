#!/usr/bin/env python
# encoding: utf-8
import json
from socket import socket
from unicodedata import name
from flask import Flask, request, jsonify
from flask_mongoengine import MongoEngine
from enum import Enum
from flask_sock import Sock

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
    GEO_CASED = 3,


''''
Data Models
'''


class Chatroom(db.Document):
    name = db.StringField()
    users = db.ListField(db.ReferenceField('User'))
    messages = db.ListField(db.ReferenceField('Message'))
    roomType = db.IntField()
    centerLocation = db.PointField()
    radius = db.FloatField()

    def to_json(self):
        return {
            'name': self.name,
            'users': [user.to_json() for user in self.users],
            'messages': [message.to_json() for message in self.messages],
            'roomType': self.roomType
        }


class User(db.Document):
    username = db.StringField()
    password = db.StringField()
    chatrooms = db.ListField(db.ReferenceField(Chatroom))

    def to_json(self):
        return {"username": self.username, "password": self.password, "chatroom": self.chatroom}


class Message(db.Document):
    content = db.StringField()
    user = db.ReferenceField(User)
    chatroom = db.ReferenceField(Chatroom)
    id = db.IntField()

    def to_json(self):
        return {"content": self.content, "user": self.user, "chatroom": self.chatroom}


'''
websockets
'''

# send message back to the same user


@sock.route('/chat')
def chat(socket):
    while True:
        message = socket.receive()
        socket.send(message)

# start websocket connection


@sock.route('/connect')
def connect(socket):
    user = User.objects(username=request.form['user']).first()
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
    if roomExists(name):
        return jsonify({"status": "error", "message": "Room already exists"})
    else:
        room = Chatroom(name=name, roomType=roomType)
        room.save()
        return jsonify({"status": "success", "message": "Room created"})

# user joins a room


@app.route('/room/join', methods=['POST'])
def joinRoom():
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
    # check if user is already in room
    if user in room.users:
        return jsonify({"status": "error", "message": "User is already in room"})
    # add user to room
    room.users.append(user)
    room.save()
    # add room to user
    user.chatrooms.append(room)
    user.save()
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
    return "User {} created".format(username)

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
        return jsonify({"status": "error", "message": "User does not exist"})
    # check if password is correct
    if not checkPassword(username, password):
        return jsonify({"status": "error", "message": "Password is incorrect"})
    return jsonify({"status": "success", "message": "User logged in"})


'''
Messages
'''

# user sends message to room

@app.route('/message/send', methods=['POST'])
def send_message():
    room = request.form['room']
    user = request.form['user']
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
    if User.objects(username=user).first().chatrooms.filter(name=room).count() == 0:
        return jsonify({"status": "error", "message": "User not in room"})
    room = Chatroom.objects(name=room).first()
    user = User.objects(username=user).first()
    room.messages.append(Message(user=user, message=message))
    room.save()
    return jsonify({"status": "success", "message": "Message sent"})



@app.route('/message/get', methods=['POST'])
def get_message():
    room = request.form['room']
    user = request.form['user']
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
    if User.objects(username=user).first().chatrooms.filter(name=room).count() == 0:
        return jsonify({"status": "error", "message": "User not in room"})

    roomObj = Chatroom.objects(name=room).first()
    if (len(roomObj.messages) - 1) < msgID:
        return jsonify({"status": "error", "message": f"Group {room} hasn't message with ID {msgID}" })

    msgObj = roomObj.messages[msgID]
    return jsonify(
        {
        "status": "success", 
        "message": "Message - GET",
        "user": msgObj.user,
        "content": msgObj.content, 
        })


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
