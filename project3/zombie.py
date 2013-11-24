import http.server
import socketserver

class MyHandler(BaseHTTPRequestHandler):

    def do_POST(self):
        print self.headers
        print self.raw_requestline

if __name__ == '__main__':
    HOST = 'localhost'
    PORT = 80

    httpd = socketserver.TCPServer((HOST, PORT), MyHandler)

    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.shutdown()
