#!/bin/sh
sed -i "s/\&gt;/>/g" $1
sed -i "s/\&lt;/</g" $1
