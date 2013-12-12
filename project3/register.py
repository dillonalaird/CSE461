import SocketServer
import threading
import time
import socket
import random
import os
import sqlite3 as lite
import sys
import time

con = lite.connect('addresses.db')


class register(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        ip = self.data
        print "Registering IP: {0}".format(ip)
        with con:
            cur = con.cursor()
            sec = time.time()
            query = "INSERT OR REPLACE INTO addresses values('{0}', {1})".format(ip, sec)
            cur.execute(query)
        

if __name__ == '__main__':
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 14000

    server = SocketServer.TCPServer((HOST, PORT), register)
    server.serve_forever()
