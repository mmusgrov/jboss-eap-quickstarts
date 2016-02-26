
./d.sh -a gf -f target/ejbtest.war

curl http://localhost:7080/ejbtest/rs/remote
curl http://localhost:8080/ejbtest/rs/local


