.ONESHELL:

sources := app/src/main/java/com/example/iot_hes/iotlab/MainActivity.java

.PHONY: help

all: help

# ensure symlinks are local to the source directory
_slink:
	dname=$(dir $(slink))
	lname=$(notdir $(slink))
	pushd $$dname >/dev/null
	ln -sf $$lname.$(suffix) $$lname
	popd >/dev/null

incomplete: $(sources)
	$(MAKE) suffix=incomplete _slink slink=$<
labo: incomplete

unlock:
	git-crypt unlock

complete: $(sources)
	$(MAKE) suffix=complete _slink slink=$<

solution: complete

clean:
	./gradlew cleanBuildCache clean
	rm -rf .gradle/ .idea/ app/build/ build/

reset: clean incomplete
	# No-op

# labo build
lbuild: incomplete clean
	./gradlew build

# solution build
sbuild: complete clean
	./gradlew build

linstall: lbuild clean
	./gradlew installDebug

sinstall: sbuild
	./gradlew installDebug

uninstall:
	./gradlew uninstallDebug


define _help_msg :=
Usage:

  make [target]

Targets:

  clean         remove build artifacts and specious files & dirs
  complete      prepare the source code for the 'solution' build
  help          guess what ;-)
  incomplete    prepare the source code for the 'labo' build
  labo          alias => 'incomplete'
  lbuild        build 'labo' (incomplete app)
  linstall      build & push 'labo' to device
  reset         alias => 'incomplete'
  sbuild        build 'solution' (complete app)
  sinstall      build & push 'solution' to device
  solution      alias => 'complete'
  uninstall     remove whatever app build from device
  unlock        decrypt sensitive files

endef

# the shell no-op silences 'nothing to be done...' messages
help:
	@: $(info $(_help_msg))
