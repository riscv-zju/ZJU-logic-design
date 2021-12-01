LAB ?= 4


all:
	sbt "runMain logic.system.Generator -T gcd.GCD -C gcd.EmptyConfig -td build/LAB$(LAB)"


help:
	sbt "runMain logic.system.Generator --help"

clean:
	rm -rf build target project/target *.v *.anno.json