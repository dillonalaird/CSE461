import httplib



conn = httplib.HTTPConnection("attu.cs.washington.edu")
conn.request("HEAD","/index.html")
res = conn.getresponse()
print res.status, res.reason
