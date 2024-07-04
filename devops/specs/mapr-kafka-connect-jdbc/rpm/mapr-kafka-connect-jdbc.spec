%undefine __check_files

summary:     Ezmeral Ecosystem Pack: Kafka Connect JDBC connector
license:     Hewlett Packard Enterprise, CopyRight
Vendor:      Hewlett Packard Enterprise, <ezmeral_software_support@hpe.com>
name:        mapr-kafka-connect-jdbc
version:     __RELEASE_VERSION__
release:     1
prefix:      /
group:       MapR
buildarch:   noarch
requires:    mapr-client >= 7.2.0, mapr-kafka >= 3.6.1
conflicts:   mapr-core < 7.2.0, mapr-kafka < 3.6.1
AutoReqProv: no

%description
Ezmeral Ecosystem Pack: Kafka Connect JDBC connector package
Tag: __RELEASE_BRANCH__
Commit: __GIT_COMMIT__


%clean
echo "NOOP"


%files
__PREFIX__/kafka-connect-jdbc
__PREFIX__/roles

%pre
# $1 -eq 1 install
# $1 -eq 2 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "pre install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :
if [ "$1" -eq "2" ]; then
	OLD_VERSION=$(cat __PREFIX__/kafka-connect-jdbc/kafkaconnectjdbcversion)
    if [ -d  __PREFIX__/kafka-connect-jdbc/kafka-connect-jdbc-$OLD_VERSION/conf ]; then
    	rm -rf __PREFIX__/kafka-connect-jdbc/kafka-connect-jdbc-$OLD_VERSION/conf
	fi
fi



%post
# $1 -eq 1 install
# $1 -eq 2 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "post install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

if [ -f  __PREFIX__/roles/kafka ]; then 
    rm -f __PREFIX__/roles/kafka
fi
touch __PREFIX__/roles/kafka

mkdir -p "__INSTALL_3DIGIT__"/conf

if [ "$1" = "1" ]; then
  touch "__INSTALL_3DIGIT__/conf/.not_configured_yet"
fi

if [ -f __PREFIX__/kafka-connect-jdbc/kafkaconnectjdbcversion ]; then
    rm -f __PREFIX__/kafka-connect-jdbc/kafkaconnectjdbcversion
fi
echo "__VERSION_3DIGIT__" > __PREFIX__/kafka-connect-jdbc/kafkaconnectjdbcversion




%preun
# N/A     install
# $1 -eq 1 upgrade
# $1 -eq 0 uninstall
[ -n "$VERBOSE" ] && echo "preun install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

WARDEN_KAFKA_CONNECT_CONF=__PREFIX__/conf/conf.d/warden.kafka-connect.conf
# Number of installed Kafka Connector packages
CONN_PACKAGES_NUM=$(rpm -qa | grep "mapr-kafka-connect-" | wc -l);
if [ "$1" -eq "0" ]; then
    if [ -d  __INSTALL_3DIGIT__/conf/ ]; then
        rm -Rf __INSTALL_3DIGIT__/conf/
    fi
    rm -Rf __PREFIX__/kafka-connect-jdbc/kafkaconnectjdbcversion
    if [ -f $WARDEN_KAFKA_CONNECT_CONF ] && [ $CONN_PACKAGES_NUM -eq 1 ] ;
        then
        KAFKA_VERSION=$(cat __PREFIX__/kafka/kafkaversion)
        bash /opt/mapr/kafka/kafka-${KAFKA_VERSION}/bin/connect-distributed-stop
        rm -Rf $WARDEN_KAFKA_CONNECT_CONF
        rm -Rf  __PREFIX__/conf/restart/kafka-connect-__VERSION_3DIGIT__.restart
        rm -f __PREFIX__/roles/kafka
    fi
fi

%postun
# N/A     install
# $1 -eq 1 upgrade
# $1 -eq 0 uninstall
[ -n "$VERBOSE" ] && echo "postun install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :


%posttrans
# $1 -eq 0 install
# $1 -eq 0 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "posttrans install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :


