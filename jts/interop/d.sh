WF_DEPLOY_DIR=/home/mmusgrov/source/forks/wildfly/wildfly.interop/build/target/wildfly-10.0.0.CR2-SNAPSHOT/standalone/deployments
WL_CP=/u01/app/oracle/Middleware/Oracle_Home/wlserver/server/lib/weblogic.jar   

usage() { echo "$1: Usage: $0 [-a <gf|gf2|wf>] [-f <archive>] [-t <gfgf|gfwf|wfgf >]" 1>&2; exit 1; }

itest() {
  case $1 in
  gfgf) curl http://localhost:7080/ejbtest/rs/remote/3700/gf/x ;;
  gfwf) curl http://localhost:7080/ejbtest/rs/remote/3528/wf/x ;;
  wfgf) curl http://localhost:8080/ejbtest/rs/remote/7001/gf/x ;;
  *)
  esac
}

while getopts "t:a:f:" o; do
case "${o}" in
  t) itest $OPTARG
     exit $?;;
  a) a=${OPTARG};;
  f) f=${OPTARG};;
  *) usage;;
esac
done

[ $f ] || usage "missing file"

case $a in
  gf) asadmin --port 4848 deploy --force=true $f
      asadmin --port 4948 deploy --force=true $f;;
  gf1) asadmin --port 4848 deploy --force=true $f;;
  gf2) asadmin --port 4948 deploy --force=true $f;;
  wf) cp $f $WF_DEPLOY_DIR;;
  *) usage "missing as"
esac

