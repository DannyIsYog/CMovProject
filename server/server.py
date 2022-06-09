#!/usr/bin/env python
# encoding: utf-8
import json
from unicodedata import name
from flask import Flask, request, jsonify
from flask_mongoengine import MongoEngine
from enum import Enum
import math

app = Flask(__name__)
app.config['MONGODB_SETTINGS'] = {
    'db': 'CMov',
    'host': 'localhost',
    'port': 27017
}
db = MongoEngine()
db.init_app(app)

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
    # coordinates
    latitude = db.FloatField()
    longitude = db.FloatField()
    radius = db.FloatField()

    def to_json(self):
        return {
            'name': self.name,
            'users': [user.to_json() for user in self.users],
            'messages': [message.to_json() for message in self.messages],
            'roomType': self.roomType,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'radius': self.radius

        }


class User(db.Document):
    username = db.StringField()
    password = db.StringField()
    chatrooms = db.ListField(db.ReferenceField(Chatroom))
    # coordinates
    latitude = db.FloatField()
    longitude = db.FloatField()

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
Rooms
'''

# check if a room already exists


def roomExists(name):
    room = Chatroom.objects(name=name).first()
    if room is None:
        return False
    else:
        return True

# get rooms that the user is in range of using room radius


@app.route('/room/nearby', methods=['GET'])
def getNearbyRooms():
    user = request.args.get('user')
    user = User.objects(username=user).first()
    if user is None:
        return jsonify({"error": "User does not exist"})
    else:
        # iterate through all geo_cased rooms
        geo_cased_rooms = Chatroom.objects(roomType=RoomType.GEO_CASED)
        # initialize list of nearby rooms
        nearby_rooms = []
        for room in geo_cased_rooms:
            if user.latitude is not None and user.longitude is not None:
                if room.latitude is not None and room.longitude is not None:
                    # check if user is in range of room
                    if distance(user.latitude, user.longitude, room.latitude, room.longitude) <= room.radius:
                        nearby_rooms.append(room)
        return jsonify({"rooms": [room.to_json() for room in nearby_rooms]})


def distance(lat1, lon1, lat2, lon2):
    R = 6371  # Radius of the earth in km
    dLat = deg2rad(lat2 - lat1)  # deg2rad below
    dLon = deg2rad(lon2 - lon1)
    a = (
        math.sin(dLat / 2) * math.sin(dLat / 2) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.sin(
            dLon / 2) * math.sin(dLon / 2)
    )
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    # distance in meters
    return R * c * 1000


def deg2rad(deg):
    return deg * (math.pi / 180)

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

# checks if user has access to a room


def userHasAccess(user, room):
    return True

# user joins a room


@app.route('/room/join', methods=['POST'])
def joinRoom():
    name = request.form['name']
    user = request.form['user']
    if roomExists(name):
        # check if user has access to room
        if not userHasAccess(user, Chatroom.objects(name=name).first()):
            return jsonify({"status": "error", "message": "User does not have access to room"})
        # check if user is already in room
        if User.objects(username=user).first().chatrooms.filter(name=name).count() > 0:
            return jsonify({"status": "error", "message": "User already in room"})
        room = Chatroom.objects(name=name).first()
        user = User.objects(username=user).first()
        room.users.append(user)
        room.save()
        return jsonify({"status": "success", "message": "User joined room"})
    else:
        return jsonify({"status": "error", "message": "Room does not exist"})

# user leaves a room


@app.route('/room/leave', methods=['POST'])
def leaveRoom():
    name = request.form['name']
    user = request.form['user']
    if roomExists(name):
        # check if user is not in room
        if User.objects(username=user).first().chatrooms.filter(name=name).count() == 0:
            return jsonify({"status": "error", "message": "User not in room"})
        room = Chatroom.objects(name=name).first()
        user = User.objects(username=user).first()
        room.users.remove(user)
        room.save()
        return jsonify({"status": "success", "message": "User left room"})
    else:
        return jsonify({"status": "error", "message": "Room does not exist"})

# gets all rooms


@app.route('/room/get/all', methods=['GET'])
def get_rooms():
    rooms = Chatroom.objects()
    return jsonify([room.to_json() for room in rooms])

# gets rooms with a user and all public rooms


@app.route('/room/get/user', methods=['GET'])
def get_user_rooms():
    user = User.objects.get(username=request.args.get("username"))
    # get user coordinates from request
    latitude = request.args.get("latitude")
    longitude = request.args.get("longitude")

    rooms = Chatroom.objects(users=user)
    # get all public rooms
    public_rooms = Chatroom.objects(roomType=RoomType.PUBLIC)
    rooms = rooms + public_rooms
    return jsonify({"rooms": [room.to_json() for room in rooms]})

# deletes a room


@app.route('/room/delete', methods=['DELETE'])
def delete_room():
    print(request.args.get("name"))
    # remove chatroom from users that were in it
    for user in User.objects():
        user.chatrooms.remove(Chatroom.objects(
            name=request.args.get("name")).first())
        user.save()
    Chatroom.objects(name=request.args.get("name")).delete()
    return "Room {} deleted".format(request.args.get("name"))


'''
Users
'''
# creates a user


@app.route('/user/create', methods=['PUT'])
def create_user():
    print(request.args.get("username"))
    print(request.args.get("password"))
    User(username=request.args.get("username"),
         password=request.args.get("password")).save()
    return "User {} created".format(request.args.get("username"))

# delete user


@app.route('/user/delete', methods=['DELETE'])
def delete_user():
    print(request.args.get("username"))
    # remove user from all rooms
    for room in Chatroom.objects():
        room.users.remove(User.objects(
            username=request.args.get("username")).first())
        room.save()
    User.objects(username=request.args.get("username")).delete()
    return "User {} deleted".format(request.args.get("username"))


'''
Messages
'''

# user sends message to room


@app.route('/message/send', methods=['PUT'])
def send_message():
    print(request.args.get("username"))
    print(request.args.get("message"))
    print(request.args.get("chatroom"))
    user = User.objects.get(username=request.args.get("username"))
    chatroom = Chatroom.objects.get(name=request.args.get("chatroom"))
    message = Message(content=request.args.get("message"),
                      user=user,
                      chatroom=chatroom,
                      id=len(chatroom.messages) + 1)
    message.save()
    chatroom.messages.append(message)
    chatroom.save()
    return "Message {} sent to room {}".format(request.args.get("message"), request.args.get("chatroom"))


# get new messages sent to a room since last message


@app.route('/message/get/new', methods=['GET'])
def get_new_messages():
    chatroom = Chatroom.objects.get(name=request.args.get("name"))
    last_message = Message.objects.get(
        content=request.args.get("last_message"))
    return jsonify({"messages": [message.to_json() for message in chatroom.messages if message.id > last_message.id]})


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
