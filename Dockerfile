FROM centos:7
ENV PROJECT_HOME "/home/fileBridge/"
ENV PROJECT_BIN "/home/fileBridge/bin"
RUN mkdir $PROJECT_HOME
RUN mkdir $PROJECT_BIN
#把项目bin目录下的文件复制到/home/fileBridge/bin
ADD bin $PROJECT_BIN

ENV RUNDIR "/home/fileBridge/bin/native"
WORKDIR $RUNDIR
RUN echo "------------starting---------"

CMD ./fileBridge

RUN echo "------------running---------"