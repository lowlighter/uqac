# Setup a small datacenter with VMware <sub>from 0x41 to 0x5A</sub>

> To do list (for the guy who repairs printers) :
> * Setup NTP
> * Setup Router
> * Setup FreeNAS + Chap
> * Create DVS and networks (vMotion, storage, production)
> * Test a VM inside production network
> * Check for certificates (?)
> * Reread to correct errors
> * Add more memes 

## Requirements

#### Computer
Your computer's CPU should have virtualisation enabled (press `Ctrl + Alt + Suppr`, open *Task manager*, click on the CPU tab and check the status of the *virtualisation* field).

![CPU virtualisation](/imgs/proc_virtu.png?raw=true)

Also, I cannot give you minimal specs for your computer, but the more you have, well, the better it is.
These are the specs of the computer where I installed everything :
```
OS  : Windows 10 Professional
CPU : Intel(R) Core(TM) i7-6600U CPU @ 2.60Ghz 2.81 GHz
RAM : 16.0 Gb
```

#### [VMware Workstation 15.0](https://www.vmware.com/products/workstation-pro/workstation-pro-evaluation.html)
VMware Workstation is a software which allows to run virtual machines (VM) on a host operating system (OS).
In this tutorial, it will be used to create VMs.

#### [VMware vSphere Hypervisor 6.7](https://my.vmware.com/web/vmware/details?downloadGroup=ESXI67U2&productId=742&rPId=33977)
vSphere contains ESXi, which is an hypervisor. An hypervisor is a special type of OS specialized in virtualization which allows to perform type 1 virtualization. 

#### [VMware vCenter Server 6.7](https://my.vmware.com/web/vmware/details?downloadGroup=VC67U2A&productId=742&rPId=33977)
vCenter contains the vCenter Server Appliance (VCSA) which acts as a management server. It makes it easier to manage ESXi and other virtualized equipements. Usually, the VCSA is also virtualized to make maintenance easier.

#### [CentOS 7.0](https://www.centos.org/download/)
CentOS is a common linux distribution, usually used for servers. 
In this tutorial, it will be used to make DNS servers.

#### [Debian 9.9](https://cdimage.debian.org/debian-cd/current/amd64/iso-cd/)
Debian is a common linux distribution.
In this tutorial, it will be used to make a NTP server.

#### [VyOS 1.2.0](https://downloads.vyos.io/?dir=rolling/current/amd64)
VyOS is a lightweight linux-based distribution which is aimed to be used for routeurs and firewalls.
In this tutorial, it will be used as the default gateway for internet as well as interconnecting the multiple private networks that will be created together.

#### [FreeNAS 11.2](https://freenas.org/download-freenas-release/)
FreeNAS is an OS specialized in storage, which allows to create a centralized and easily accessible place for data.
In this tutorial, it will be used as a datastore.

### Edito
In this tutorial, I tried to detail almost everything but it is sure that you'll understand what you're doing better if you already know the things a little bit.

I assume that you're aware of what exists in networking and what are the basics. 
Especially things like MAC and IPs addresses, netmasks, routes aggregation, etc.

Also, you can see that various *.iso* was used to setup equipements, and some stuff from this tutorial may not always be revelent, but note that this was also because I wanted to test various stuffs and work in a various environment. 

Note that the aim of this tutorial is to setup a small datacenter and is mainly focused on how to do so. 
You probably won't be able to really use it if you don't have the right equipement, and anyway a datacenter becomes more interesting as the scale goes up. Remember that (especially if you're doing it on a laptop because you're too poor to afford real equipments), this lab will push your computer in its extremes limits...

Finally, a little disclaimer, I cannot pretend to be an expert (atm) of datacenters, virtualization, networks, etc. but feel free to comment and contribute to this tutorial !

Well, have a good read :) !

## Architecture

![Proposed architecture](/imgs/architecture.png?raw=true)

Below is a recap table of which IPs and domains names are associated to which functions.

| IP address | Domain name   | Description                 |
| ---------: | ------------: | --------------------------- |
|   10.0.0.1 |               | Your computer (VMware host) |
|   10.0.0.7 |   gw.my.local | Routeur (VyOS)              |
|   10.0.0.8 | dns0.my.local | Master DNS (BIND on CentOS) |
|   10.0.0.9 | dns1.my.local | Slave DNS (BIND on CentOS)  |
|  10.0.0.10 | dns0.my.local | Master DNS (BIND on CentOS) |
|  10.0.0.11 |  ntp.my.local | NTP server (Debian)         |
|  10.0.0.12 |  nas.my.local | NAS (FreeNAS)               |
| 10.0.0.100 | esx0.my.local | ESXi - Unit 0               |
| 10.0.0.101 | esx1.my.local | ESXi - Unit 1               |
| 10.0.1.100 |               | ESXi - Unit 0               |
| 10.0.1.101 |               | ESXi - Unit 1               |
| 10.0.2.100 |               | ESXi - Unit 0               |
| 10.0.2.101 |               | ESXi - Unit 1               |
| 10.0.3.100 |               | ESXi - Unit 0               |
| 10.0.3.101 |               | ESXi - Unit 1               |

## Setup DNS servers

### Setup a master DNS server

*Note that in this tutorial, I will setup both a master DNS server and a slave DNS server.
You can skip the slave DNS server if you want. If so, just need omit all `dns1.my.local` entries in the following part.*

#### Install BIND

The DNS server is a primordial component in your architecture : it will allow your machines to resolve each other.
In this tutorial, I'll use a [CentOS](https://www.centos.org/) to build a DNS.

First, create a new VM and put the *CentOS-xxxx.iso* in the CD drive. 
To make things easier for now, connect it with a *Bridged* adapter (this way we'll have access to internet and be able to install packages).
 
Once you're done with setup, login to your machine and use `loadkeys fr` if you're using an AZERTY keyboard.

Ensure the machine got an IP with `ip addr` and try a simple `ping github.com` to see if everything is working. 
If not, try using the DHCP client with `sudo dhclient`.

![DNS Initialisation](/imgs/dns_init.png?raw=true)

Now, download and install [BIND](https://en.wikipedia.org/wiki/BIND), which is a well-known DNS software for linux distributions :
`sudo yum install bind bind-utils -y`.

#### Edit BIND settings

Then type `vi /etc/named.conf` which will open the settings for the BIND name server.

We'll need to edit a few parts :
* In `listen-on port 53`, add the static IPs you chose for your DNS. For exemple : `{ 127.0.0.1; 10.0.0.8; }`.
* In `allow-query`, add the subnet from which clients are allowed to make queries. For exemple : `{ localhost; 10.0.0.0/21; }` (notice the CIDR set to `/21`, which refers to the ip range `10.0.0.0` through `10.0.7.255`).
* If you intend to setup a slave DNS, in `allow-transfer`, add the IPs of your slaves DNS. For exemple : `{ localhost; 10.0.0.9; }`. If not, you should just setup this option to `{ localhost; }` 
* You may also disable the `listen-on-v6` by putting a `#` just before.

Also, we would like our DNS to solve others domains from the internet.
For this, you can add the following lines :
```
forward only;
forwarders { 10.0.0.7; }; 
//This tells our DNS where to lookup when it doesn't know the answer to the query.
//You can put directly another DNS server, but you can also (which is what I did above) put the router's IP.
```

Lower in the file, you'll find a default `zone` block. 
Create a new `zone` block just below with the following configuration:
```c
//Define the "my.local" zone
zone "my.local" IN {
  type master; //Tell that the zone is handled by a master DNS.
  file "forward.my"; //Path to zone file definition
  allow-update { none; }; //Prevent client from making additional DNS entries
};
```
The above will define where the server should lookup infos about the `my.local` domain name.
This way, machines will be able to translate domain names to IPs (e.g. `esxi0.my.local` to `10.0.0.100`). 

However, we would also like that machines are able to do the opposite (i.e. `10.0.0.100` to `esxi0.my.local`), which is why we'll also create a reverse zone.

```c
//Define the reverse "my.local" zone
//You need to write the subnet in reverse (e.g. 10.0.0 becomes 0.0.10)
//To avoid making multiples reverse zones definition, we will group 10.0.X subnets into a single file.
zone "0.10.in-addr.arpa" IN {
  type master; //Tell that the reverse zone is handled by a master DNS.
  file "reverse.my"; //Path to reverse zone file definition
  allow-update { none; }; //Prevent client from making additional DNS entries
};
```

Type `named-checkconf /etc/named.conf` to check if there is no errors in the configuration file. 
If everything is ok, you can go to next step.

#### Create zones files.

Now, we will create and fill the referenced files we defined above.
They will contain the actual DNS informations concerning machines on our network.

Type `vi /var/named/forward.my` and to configure the forward zone file like below :

```vhdl
$TTL 1h

;-- The Start of Authority (SOA) is the name of the authoritative master name server for the zone, followed by the email of the responsible of the server
;-- Behind are the serial number (usually an ISO timestamp), slave refresh period, slave retry time, slave expiration time, and the maximum time to cache the record
@       IN  SOA   dns0.my.local.    root.my.local.    (2019060101 2h 1h 1w 1h)

;-- Nameservers
@       NS    dns0.my.local.
@       NS    dns1.my.local.

;-- Records
gw      A     10.0.0.7
dns0    A     10.0.0.8
dns1    A     10.0.0.9
vcsa    A     10.0.0.10
ntp     A     10.0.0.11
nas     A     10.0.0.12
esxi0   A     10.0.0.100
esxi1   A     10.0.0.101
```

Now type `vi /var/named/reverse.my` to configure the reverse zone file.

```vhdl
$TTL 1h

;-- Start of Authority
@       IN  SOA   dns0.my.local.    root.my.local.    (2019060101 2h 1h 1w 1h)

;-- Nameservers
@       NS    dns0.my.local.
@       NS    dns1.my.local.
@       PTR   my.local.

;-- Records
dns0    A     10.0.0.8
dns1    A     10.0.0.9
0.7     PTR   gw.my.local.
0.8     PTR   dns0.my.local.
0.9     PTR   dns1.my.local.
0.10    PTR   vcsa.my.local.
0.11    PTR   ntp.my.local.
0.12    PTR   nas.my.local.
0.100   PTR   esxi0.my.local.
0.101   PTR   esxi1.my.local.
```

Type `named-checkzone my.local /var/named/forward.my` and `named-checkzone my.local /var/named/reverse.my` to check if there are no errors in the zone file. 

![Check zones](/imgs/dns_named_check.png?raw=true)

#### The last details

Before we can start using the DNS, we need to start the DNS service by typing the following commands (the `systemctl enable` allows the service to start on boot) : 

```bash
systemctl enable named
systemctl start named
```

Then, ensure that the firewall won't block DNS queries which will arrive on port 53 by typing the following commands : 
```bash
firewall-cmd --zone=public --add-service=dns
firewall-cmd --reload
```

We will now update permissions (group and owner) to avoid problems with the service :
```bash
chgrp named -R /var/named
chown -r root:named /etc/named.conf
restorecon -rv /var/named
restorecon /etc/named.conf
```

Now open `vi /etc/resolv.conf`, which contains the list of DNS server used by the OS to resolve name. 
Edit the file to put the IPs of the created DNS : 
```bash
nameserver 127.0.0.1
nameserver 10.0.0.8
```

Then open the configuration of the VM's network interface `vi /etc/sysconfig/network-scripts/ifcfg-ens33` (the interface name may change depending on you machine).

Set a static IP : 
```bash
BOOTPROTO=static
IPADDR=10.0.0.8
NETMASK=255.255.255.0
ONBOOT=yes
```

Update the DNS and the default gateway to our own : 
```
DNS=10.0.0.8
GATEWAY=10.0.0.7
PEERDNS=no
```

Notice the `PEERDNS` option, which will prevent the `/etc/resolv.conf` to be overwritten.

Finally, move the network adptater from *bridged* to our *host-only* `10.0.0.0` network from VMware and restart network with `systemctl restart network`.
Then ensure with `ip addr` that you have the correct IP (the one from the private network).

![Change virtual network adaptater](/imgs/dns_bridged_change.png?raw=true)

![Check IP](/imgs/dns_ip_check.png?raw=true)

#### Test if everything works 

Finally, you can test the DNS with `dig`. 
For exemple, try the command `dig nas.my.local` to resolve the `nas.my.local` url, which should sent back the following : 

![Check IP](/imgs/dns_lookup.png?raw=true)

You can also try a reverse lookup with the `-x` option, like in `dig -x 10.0.0.12`.

Does everything works ?
If yes, great we've got a working master DNS !

### Setup a slave DNS server (optional)

#### Quick start

Since creating a slave DNS is pretty similar to creating a master DNS, just create a clone of the VM on which you setup your main DNS.
Power off you first DNS while configuring the second to avoid problems.

#### Edit slave configuration

Type `vi /etc/named.conf` to open the settings for the BIND name server.

Edit the following fields :
* In `listen-on port 53`, add the static IPs you chose for your salve DNS. For exemple : `{ 127.0.0.1; 10.0.0.9; }`
* Remove the IPs of this server from `allow-transfer`.

Lower in the file, you'll need to edit the `zone`s blocks to tell that the zone are handled by a slave DNS.
```c
//Define the "my.local" zone
zone "my.local" IN {
  type slave; //Tell that the zone is handled by a slave DNS.
  file "slaves/forward.my"; //Path to zone file definition
  masters { 10.0.0.8; }; //IPs of masters DNS
};

//Define the reverse "my.local" zone
zone "0.10.in-addr.arpa" IN {
  type slave; //Tell that the zone is handled by a slave DNS.
  file "reverse.my"; //Path to reverse zone file definition
  masters { 10.0.0.8; }; //IPs of masters DNS
};
```
Type `named-checkconf /etc/named.conf` to check if there is no errors in the configuration file. 

Open the configuration of the VM's network interface `vi /etc/sysconfig/network-scripts/ifcfg-ens33`, and change the static IP to one different from master DNS. It is also a good idea to replace the UUID by a new one by using the `uuidgen` command. 

```bash
IPADDR=10.0.0.9
DNS1=10.0.0.8
DNS2=10.0.0.9
UUID=(output of uuidgen command)
```

Then, restart network with `systemctl restart network`. 
As always, you can check with `ip addr` that you have the correct IP.

Finally, power on the master DNS, and you should be able to test the architecture.

## Setup router

#### Create the virtual machine and setup VyOS

For now, we only had one network (`10.0.0.0/24`), so the layer 2 network was sufficient for our two DNSs to communicate.
However, we'll later need to create other networks for production, migration and storage of our datacenter.
To connect these differents network, we'll need to setup a router. 
Moreover, if we also want an access to the internet, that's one more reason.

Create a new VM with 512Mb RAM, 2Gb HD and 2 networks adapters. 
Set the first one to *host-only* (i.e. on the `10.0.0.0` network) and the second one to *bridged*, and tick the *Replicate physical network connection state* (this way the IP will be renewed each time your computer move to another network, which is especially useful if you're on a laptop).

Put the `VyOS.iso` into the CD drive and start the VM.
Then, login with the default credentials which are both `vyos` for the username and the password. 

Type `install image` to install the image on the hard drive.
Answer the questions asked by the installer and once it tells that the OS has been installed, type `poweroff`, disconnect the CD drive and restart the VM.

![VyOS setup](/imgs/vyos_setup.png?raw=true)

If you have an AZERTY keyboard, type `sudo dpkg-reconfigure keyboard-configuration` to open the keyboard configuration menu, then select `Generic 105-Key (Intl) PC`, then `Other`, `French` and finally `No compose key`.

#### Configure network interfaces

Type `ip addr` to check the IPs of the network interfaces.

![VyOS ip addresses](/imgs/vyos_ifcfg_ip.png?raw=true)

You'll need to now to which interface is connected which network adapter.
Usually, the first one connected is the `eth0` and etc. in order, but it may not always be the case. 
So just make a quick-check with the MAC addresses (by looking up on VMware Workstation in the *Advanced* settings of the network adpaters of the VM).

![VyOS mac addresses](/imgs/vyos_ifcfg.png?raw=true)

In my case, here are my settings. 
In your case, the network to set may change depending on how you setup the network adapters : 
the *Host-only* is the one we want to connect to our private network and the *Bridged* one is for accessing your current computer's network.

| Interface | MAC address       | Network adapter | Network we would like to set    |
| --------- | ----------------- | --------------- | ------------------------------- |
| eth0      | 00:0C:29:A2:3B:BB | Host-only       | 10.0.0.0                        |
| eth1      | 00:0C:29:A2:3B:C5 | Bridged         | Same as your computer's network |

The ways VyOS works is a bit like git : you make changes, you commit them, and save them if you're satisfied. 
You can rollback to a previous state if needed by using the `compare` and `rollback` commands.

Anyway, enter in configuration mode by typing `configure`.

So for the *Host-only* (`eth0` in my case), we want to setup a static ip, which is `10.0.0.7/24`. 
Type the following to make it happen :

```bash
set interfaces ethernet eth0 address 10.0.0.7/24
set interface ethernet eth0 description LAN
```

Now for the *Bridged* (`eth1` in my case), we'll turn on DHCP for this interface as the existing network should give us an IP.

```bash
set interfaces ethernet eth1 address dhcp
set interface ethernet eth1 description WAN
```

We also need to configure NAT if we want the management network (`10.0.0.0`) and the production network (`10.0.1.0`) to be connected the internet.
Create a NAT rule with the outbound interface set to *Bridged* interface (the `100` is just an arbitratry id) :

```bash
set nat source rule 100 outbound-interface eth1
set nat source rule 100 source address 10.0.0.0/23
set nat source rule 100 translation address masquerade
set nat source rule 100 description NAT-LAN-TO-WAN
```

Then commit and save your changes, and exit the configuration mode with :

```bash
commit
save
exit
```

Type `show interfaces` to see if all worked.
The *Bridged* interface should have received an IP from the same network as your computer trhough DHCP.

![VyOS interfaces](/imgs/vyos_interfaces.png?raw=true)

#### Configure DNS

Now go back to configuration mode with `configure`, we'll finish our DNS settings.

First of all, let tell our router that the `my.local` domain is handled by our DNS :
```bash
set service dns forwarding domain my.local server 10.0.0.8
set service dns forwarding domain my.local server 10.0.0.9
```

The DNS we configured previously forwards the requests to the routeur if they don't know the answer.
We'll set the routeur to also forwards queries to another DNS like the Google's public ones : 

```bash
set service dns forwarding name-server 8.8.8.8
set service dns forwarding name-server 8.8.4.4
set service dns forwarding listen-address 10.0.0.7
```

Note that we needed to precise on which interface the DNS queries might arrive, which is on our private network interface, so the `10.0.0.7`.

Finally, add the DNS servers entries to our VyOS : 
```bash
set system name-server 10.0.0.8
set system name-server 10.0.0.9
```

Then again, commit your changes and save them.
If you need to see all of your configuration, type `nano /config/config.boot.`

#### Test the installation

Switch to one of the DNS VM, and try to ping a `my.local` domain and also an exterior domain, like `github.com` :

![Test routeur](/imgs/router_test.png?raw=true)

Well done ! 

## Setup NTP server

#### Create the virtual machine and setup Debian

Create a new VM and put the `debian-xxxx.iso` into the CD drive, power on the VM and start the install.

When asked to configure the network, set manually the IP to `10.0.0.11` and the mask to `255.255.255.0`, the IP of the gateway to `10.0.0.7` and the name servers to `10.0.0.8 10.0.0.9`.

Then set the hostname to `ntp`, the domain name to `my.local`.

Configure accounts, choose how the disk will be partitionned and finalize the setup. 
Don't bother too much with it, as the purpose of this VM is just to be time server. 

## Setup storage

#### Create the virtual machine and setup FreeNAS

Create a new VM with `FreeNAS-XXXX.iso` in the CD player. 
Choose a at least 8Gb RAM, create three HD, one of 8Gb for the OS and the last two for the storage with a reasonable amount of space, like 200 Gb each (anyway this won't be "real" 200 Gb).

When prompted, type `1` to start the installation. 
As always, follow instructions. 
You may have to setup the `BIOS` option instead of the `UEFI` one because it could lead to the OS not booting up properly when you restart the VM. Be careful, sometimes the CD player stays active so you may have to manually disable it.

Anyway, once you're done rebooting, you'll have access to a console.

![FreeNAS console](/imgs/nas_console.png?raw=true)

Type `1` to access network interface configuration.
Set the IPv4 to `10.0.0.12` and the mask to `/24`.

![FreeNAS network configuration](/imgs/nas_network.png?raw=true)

Now, open your browser and type `10.0.0.12`, which will lands you on the FreeNAS web interface, from where you can configure it.

![FreeNAS web interface](/imgs/nas_web.png?raw=true)

#### Create a pool

First we'll create a storage pool.
In the left menu, click on *Storage*, *Pools* and create a pool. 
You can read the [documentation](https://ixsystems.com/documentation/freenas/11.2-U4.1/storage.html) about pools to know more about it.
If you don't really know what to do, just click on *SUGGEST LAYOUT* and confirm the pool creation.

![FreeNAS add pool](/imgs/nas_pool.png?raw=true)

On the created pool, click on the dot menu and choose *Add Zvol* to create a Zvol, which can be used as a ISCSI device (we'll take about it later).

You can choose several options or make them inherit from the pool.
Usually Zvols must not occupy more than 80% of the pool (to prevent excessive fragmentation with ISCSI), so it will fail if you try to allocated the entire space of the pool.
Especially, you can set compression (which as the name implies, compress data to save storage space) and deduplication (which allows to reuse duplicated data to save space). The later one is RAM intensive (~5Gb RAM for 1T of data) and as you know, I'm a little short on RAM so I'll opt out for this one. Nevertheless you can experiment a lot with FreeNAS as it is well documented.

![FreeNAS create Zvol](/imgs/nas_zvol.png?raw=true)

Finally, your pool should look like this :

![Joke](/imgs/joke_pool.png?raw=true)

Ok, jokes aside, it should look like this :

![FreeNAS pool](/imgs/nas_pool.png?raw=true)

#### Setup iSCSI

We'll use the [ISCSI](https://fr.wikipedia.org/wiki/ISCSI) protocol for our storage.
ISCSI exports disk devices over an Ethernet network that initiators (clients) can attach and mount. 
Usually, you use a fibre channel infrastructure and a dedicated networks specifically designed for ISCSI traffic.

In the left menu, go to *Services* and enable ISCSI.

![FreeNAS web interface](/imgs/nas_iscsi.png?raw=true)

Then, expand the *Sharing* menu and click on *Block (ISCSI)*.
First, let's create a portal.
A portal specifies which IP address and port to use for the ISCSI.
Set the network interface we'll use (`10.0.0.12`). 
Let the authentification to `NONE` for now.

![FreeNAS add portal](/imgs/nas_iscsi_portal.png?raw=true)

The next step is to configure authorized initiators (systems which are allowed to connect to ISCSI targets).
Let's restrict the authorized networks to the storage network, which is `10.0.3.0/24`.

![FreeNAS add initiators](/imgs/nas_iscsi_initiators.png?raw=true)

Next, we'll create a target. A target is a storage resource on the system. 
Each target has a unique ISCSI Qualified Name (IQN) which is used as an identifier, and is associated to a portal, initiators and an authentification method.

Note that target creates a block device which may be accessible by multiple initiators : this means that a clustered filesystem is required (ESXi use VMFS which is one) because multiples clients may have R/W access at the same time. If you're using a more traditional FS (EXT, FAT, NTFS, ...), you must ensure that only one initiator have the R/W access at a time, or the FS will be corrupted.

![FreeNAS add target](/imgs/nas_iscsi_target.png?raw=true)

Then we need to create an extent, which is define what resources are shared with client. 
It can be either a file or a device (the later can be a disk, an SSD, etc.).
In our case, it will be the Zvol we created previously, as these type of extents provide the best performance and most features for VM initiators.

![FreeNAS add extent](/imgs/nas_iscsi_extent.png?raw=true)

Finally, the last step is to associate a target to an extent, this means our storage resource to the storage unit to be shared.
For this, click on *Associated targets*, select the target and the extent we created, and choose a unique number to design the unit (LUN ID), for example `0`.

![FreeNAS add assocated target](/imgs/nas_iscsi_associated.png?raw=true)

Now the storage system is ready ! We'll link them to our ESXis a bit later.

## Setup ESXis

#### Install and configure a ESXi

Create a new VM with `VMware-VMvisor-Installer-XXXX.iso` in the CD player. For `esxi0`, choose at least 10Gb RAM, 2 CPUs and 300 Gb for the HD, because these are the requirements to install VSCA. You can choose lower hardware for `esxi1`.

Once you boot up, just follow the instructions for installation.
When the hypervisor is ready, press `F2` to access configuration menu. 

Go to the *Configure management network* section.
Open the *IPv4 Configuration*, to set a static IP `10.0.0.10X` and a mask `255.255.255.0` (chose the IPs defined in the DNS, so `10.0.0.100` for `esxi0` and `10.0.0.101` for `esxi1`). Set the default gateway to `10.0.0.7`.

Then, in *DNS Configuration*, add the IPs of the IPs of the two DNS `10.0.0.8` and `10.0.0.9` we created before, and set hostname to  `esxi0` or `esxi1` depending on the unit.

Finally, use the *Test management network* to ensure that everything works. 
This should ping the two DNS and resolve its own name.

![Test ESXi setup](/imgs/esxi_setup.png?raw=true)

Then, repeat the operation for the second ESXi.

#### Install vCenter Server Appliance 

First you need to open `VMware-VCSA-all-XXXX.iso` on your computer. Navigate through `/vcsa-ui`, select your os and launch the installer.

![VCSA installer](/imgs/vcsa_init.png?raw=true)

Then click on install which will land you on this window :

![VCSA installation](/imgs/vcsa_setup.png?raw=true)

Select a topologie for your deployment (see [VMware knowledge base](https://kb.vmware.com/s/article/2108548) for more informations).
Honestly, I just chose the embedded one because it looked easier to setup.

![VCSA deployment](/imgs/vcsa_deployment.png?raw=true)

Select the target where to install the VCSA. This is the ESXi where you want to install the VCSA.
Choose `10.0.0.100` and enter the credentials.

![VCSA target](/imgs/vcsa_appliance.png?raw=true)

Configure the VM which will run the VCSA.

![VCSA vm](/imgs/vcsa_appliance.png?raw=true)

Then, you will be asked the applicance size. 
Personally, I chosed *tiny* because I doubt that my 16Gb RAM computer can handle more than 10 hosts at once...

![VCSA size](/imgs/vcsa_size.png?raw=true)

After that, select a datastore where the VCSA will reside. 
Don't forget to tick the *Thin Disk Mode* (this way only occuped space will be allocated).

![VCSA size](/imgs/vcsa_storage.png?raw=true)

You will be asked several network settings.
Type `10.0.0.10` for both the IP and FQDN (and `24` fort the mask), `10.0.0.7` for the default gateway and `10.0.0.8, 10.0.0.9` for the DNS field. 

Technically you could choose to set the FDQN to `vcsa.my.local` (or empty and solve it by reverse lookup), as our DNS is properly configured. However, you may choose `10.0.0.10` to avoid adding our custom DNSs in your DNS list of your computer main OS.

![VCSA network](/imgs/vcsa_network.png?raw=true)

Review your settings and finish the installation of the VCSA.

![Recap](/imgs/vcsa_recap.png?raw=true)

Once you're finished with the stage 1 installation, the installer will ask you if you want to setup stage 2 now. 
Answer in the affirmative to land on this new page :

![Stage 2](/imgs/vcsa_init2.png?raw=true)

You can either synchronize the VCSA clock with the host (`esxi0` or with the NTP server `10.0.0.11`). 
As the host will be connected to the NTP server, it doesn't really matter so you can leave this option to synchronize with host.
You can choose to enable SSH if you want.

![VSCA configuration](/imgs/vcsa_config.png?raw=true)

Now, you can create or join a SSO (Single Sign-On) domain, which will be used later by vSphere to handle all authentifications.

![VSCA SSO](/imgs/vcsa_sso.png?raw=true)

Review last settings and finish installation.

![VSCA review](/imgs/vcsa_review.png?raw=true)

Once you finished the installation of the VCSA, type `10.0.0.10` in your browser to access the vSphere Web client, enter your credentials and Voilà ! You can now manage easily you architecture.

![VSCA web interface](/imgs/vcsa_web.png?raw=true)

## Setup the datacenter

### Create the cluster 

In the left menu of the vSphere client, select *Hosts and clusters*.
Normally, you should only have your VCSA referenced as `10.0.0.10`, and that's it.

Right click on it, and create a new datacenter. 
Then right click on the datacenter you created and create a new cluster.

For the cluster, there are several options : 
* The DRS allows ESXi to have distributed shared resources. 
* The *vSphere HA* allow to have an high availability on VMs by monitoring them continually.
* The vSAN is a software-defined storage which allow to create distributed and shared data stores.

![Create a cluster](/imgs/cluster_create.png?raw=true)

For now, you can opt out all these options. 
You can enable them later.

Then, the next step is to add the two ESXi we created in the previous step.
Right click on the cluster, select *Add hosts*, and type the IPs and the credentials of the ESXis.

![Add hosts to cluster](/imgs/cluster_create1.png?raw=true)

At next step, you may have a warning about having a VM running on `esxi0`. 
Don't worry about it, the VSCA will remain active while you perform the action. 

![Add host to cluster](/imgs/cluster_create1.png?raw=true)

It is possible that the `esxi1` doesn't come out of maintenance mode alone, so right click on it and exit the maintenance mode manually.

After all this steps, you should have your datacenter which looks like that : 
![Datacenter](/imgs/datacenter.png?raw=true)

Pretty nice !

> ...
_________________________________________________


### References

Below are a list of resources which helped me through this little journey.

* M. Ploix from l'[Université de technologie de Troyes](https://www.utt.fr/)
* [opensource.com](https://opensource.com/article/17/4/build-your-own-name-server)
* [unixmen.com](https://www.unixmen.com/setting-dns-server-centos-7/)
* [virtubytes.com](http://www.virtubytes.com/2017/01/26/install-vcsa-6/)
* [vyos.net](https://wiki.vyos.net/wiki/User_Guide)
* [ixsystems.com](https://ixsystems.com/documentation/freenas/11.2-U4.1/sharing.html#block-iscsi)

![To be continued](/imgs/to_be_continued.png?raw=true)
