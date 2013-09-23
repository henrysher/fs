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

grep -q " verbose" /proc/cmdline && dmesg -n 8 || dmesg -n 3

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

echo Setting random seed
[ -f /etc/random-seed ] && cat /etc/random-seed >/dev/urandom
dd if=/dev/urandom of=/etc/random-seed count=1 bs=512 2>/dev/null

echo Storing dmesg output to /var/log/dmesg.log
dmesg > /var/log/dmesg.log

/etc/rc.multi
