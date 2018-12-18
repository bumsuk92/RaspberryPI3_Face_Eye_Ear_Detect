# -*- coding: utf-8 -*-

import socket
from threading import Thread

class ConnectServer:
    def __init__(self):
        self.host = '70.12.113.141'
        self.port = 6001
        self.autoFlag = 1
        self.pushCheckFlag = 1

    def sendMsg(self, msg):
        self.sock.sendall(msg.encode())

    def connectToServer(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((self.host, self.port))
        self.sendMsg("$;0003;S;on;&")

    def receiveMsg(self, sock):
        while True:
            try:
                data = sock.recv(1024)
                if not data:
                    break

                if data.decode() == "$;server;00;;&":
                    self.autoFlag = 1
                    self.sendMsg("$;0003;02;;&")

                elif data.decode() == "$;server;01;;&":
                    self.autoFlag = 0
                    self.sendMsg("$;0003;03;;&")
                    
                elif data.decode() == "$;server;A2;;&" or "$;server;A3;;&" :
                    self.pushCheckFlag = 1
            except:
                pass

    def connectClose(self):
        self.sendMsg("$;0003;E;;&")
        self.sock.close()
