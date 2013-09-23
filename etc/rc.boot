#!/bin/sh

. /etc/rc.conf

echo
echo
echo "         Morpheus booting"
echo
echo

echo Mounting filesystems
mkdir -p /dev/pts /dev/shm
mkdir -p /sys
mkdir -p /proc
mount -n -t proc proc /proc
mount -n -t sysfs sysfs /sys
mount -n -t devpts devpts /dev/pts

echo Running smdev
smdev -s

echo Setting smdev as the kernel hotplug
echo /bin/smdev > /proc/sys/kernel/hotplug

echo "Setting hostname to $HOSTNAME"
hostname $HOSTNAME

ifconfig lo up

HWCLOCK_PARAMS="-s"
case $HARDWARECLOCK in
	"")
		;;
	UTC)
		HWCLOCK_PARAMS="${HWCLOCK_PARAMS} -u"
		;;
	localtime)
		HWCLOCK_PARAMS="${HWCLOCK_PARAMS} -l"
		;;
	*)
		HWCLOCK_PARAMS=""
		;;
esac

if [ -n "$HWCLOCK_PARAMS" ]; then
	echo Setting hwclock
	[ -n "$TIMEZONE" ] && export TZ="$TIMEZONE"
	hwclock $HWCLOCK_PARAMS
	unset TZ
fi

echo Storing dmesg output to /var/log/dmesg.log
dmesg > /var/log/dmesg.log