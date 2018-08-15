#!/bin/sh
set -ex

pwd

if [ ! -f protobuf-2.4.1/Makefile ]; then
   wget https://github.com/google/protobuf/releases/download/v2.4.1/protobuf-2.4.1.tar.gz
   tar -xzvf protobuf-2.4.1.tar.gz
   cd $TRAVIS_BUILD_DIR/protobuf-2.4.1 && ./configure --prefix=/usr && 
make 
fi

cd $TRAVIS_BUILD_DIR/protobuf-2.4.1
sudo make install


