WF_DEPLOY_DIR=/home/mmusgrov/source/forks/wildfly/wildfly.interop/build/target/wildfly-10.0.0.CR2-SNAPSHOT/standalone/deployments
WL_CP=/u01/app/oracle/Middleware/Oracle_Home/wlserver/server/lib/weblogic.jar   

usage() { echo "$1: Usage: $0 [-a <gf|gf2|wf>] [-f <archive>]" 1>&2; exit 1; }

while getopts "a:f:" o; do
case "${o}" in
  a) a=${OPTARG};;
  f) f=${OPTARG};;
  *) usage;;
esac
done

[ $f ] || usage "missing file"

case $a in
  gf) asadmin --port 4848 deploy --force=true $f;;
  gf2) asadmin --port 4948 deploy --force=true $f;;
  wf) cp $f $WF_DEPLOY_DIR;;
  *) usage "missing as"
esac

