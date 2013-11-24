import SocketServer

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        print "{} wrote:".format(self.client_address[0])
        print self.data
        response = "ACK"
        self.request.sendall(response)

if __name__ == '__main__':
    HOST, PORT = '69.91.152.47', 15000

    server = SocketServer.TCPServer((HOST, PORT), zombie)
    server.serve_forever()
