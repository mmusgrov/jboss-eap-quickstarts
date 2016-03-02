
./d.sh -a gf -f target/ejbtest.war

# gf -> gf - works
curl http://localhost:7080/ejbtest/rs/remote/3700/gf/x
# gf -> wf - times out with javax.naming.CommunicationException: Cannot connect to ORB
curl http://localhost:7080/ejbtest/rs/remote/3528/wf/x 
# wf -> gf
curl http://localhost:8080/ejbtest/rs/remote/7001/gf/x 




# invoke domain1 which in turn uses jndi port 3700 to lookup an ejb on domain2:
curl http://localhost:7080/ejbtest/rs/remote/3700

curl http://localhost:7080/ejbtest/rs/remote
curl http://localhost:8080/ejbtest/rs/local

gf domain1 -> domain2
asadmin start-domain domain1 # admin port: 4848 iiop port: 7001 http port: 7080
asadmin start-domain domain2 # admin port: 4948 iiop port: 3700 http port: 8080
curl http://localhost:7080/ejbtest/rs/remote/3700

https://docs.oracle.com/cd/E19776-01/820-4496/beanm/index.html
https://docs.oracle.com/cd/E18930_01/html/821-2416/gjjpy.html

asadmin set server1.transaction-service.automatic-recovery=false

Reference Manual: https://docs.oracle.com/cd/E26576_01/doc.312/e24938/toc.htm
https://docs.oracle.com/cd/E26576_01/doc.312/e24928/transactions.htm#GSADG00606

asadmin --port 4948
asadmin> get --monitor inst1.server.transaction-service.activeids-current
remote failure: No monitoring data to report.

