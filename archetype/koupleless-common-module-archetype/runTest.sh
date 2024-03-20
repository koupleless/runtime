echo "start to install archetype "
mvn clean install
rm -rf target
mkdir target
cd target
appName=testBiz1
echo "开始初始化单 bundle 模块： testBiz1"
rm -rf testBiz1
mvn archetype:generate \
        -DarchetypeArtifactId=koupleless-common-module-archetype \
        -DarchetypeGroupId=com.alipay.sofa.koupleless \
        -DarchetypeVersion=0.0.1-SNAPSHOT \
        -DgroupId=com.alipay.sofa.koupleless.${appName} \
        -DartifactId=${appName} \
        -Dversion=0.0.1-SNAPSHOT \
        -Dpackage=com.alipay.sofa.koupleless.${appName} \
        -DarchetypeCatalog=local
#
cd testBiz1
mvn clean package
