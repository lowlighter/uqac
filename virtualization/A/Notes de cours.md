
# 🖥️ Réseau - notes de cours
_____
# Niveau 1

Historiquement, la transmission des données se faisait via le réseau électrique, avec le même câble pour les utilisateurs. La différence de potentiel était utilisée pour transmettre les données. L'inconvénient d'une telle méthode est que les données sont lisibles par tout le monde et qu'un seul utilisateur à la fois peut occuper le canal. Il y a également des problèmes d'écho qui apparaissent avec les fréquences élevées selon la longueur du câble...

Néanmoins le besoin étant de réaliser des communications asynchrones, ceci à mené à l'élaboration de stratégies d'adressage (quoi/qui, où, pourquoi) et à ce qu'on connait aujourd'hui.

# Niveau 2 
## La commutation

### L'adresse MAC
Le *Media Access Control* (MAC) est une adresse "physique" qui est liée à l'équipement, à priori unique pour chaque équipement. Elle est constitué de 6 octets, la première moitiée désignant le constructeur et la seconde le n° de série.

La longueur de l'adresse MAC permet de garantir un confort d'utilisation lors de l'utilisation du niveau 2 : il n'y a besoin de configurer aucun matériel pour que cela fonctionne.

### Le protocole Ethernet
Le protocole ethernet a pour objectif de permettre le partage d'un support de communication entre différents équipements, en utilisant une stratégie d'adressage.

Une trame ethernet contient principalement :
- L'adresse MAC de destination
- L'adresse MAC source
- Des données

### Le commutateur (switch)
Le switch lit l'entête de la trame puis se base sur le contenu de sa table de commutation pour décider quoi en faire.

Celle-ci est construite au fur et à mesure que les équipements communiquent, en associant l'adresse MAC source à un n° de prise. Il est possible d'avoir une prise associée à plusieurs adresses MAC, notamment dans le cas où un autre switch se trouve sur celle-ci.

Si trame avec une adresse MAC de destination n'est pas dans la table, la trame unicast est traité comme une trame broadcast et est diffusé sur l'ensemble des prises du switch (fallback).

Le *Spanning Tree Protocol* permet d'éviter les problèmes liés aux boucles présente dans la topologie du réseau.

Le switch possède une mémoire afin de constituer une file d'attente d'entrée et de sortie pour chaque prise. Il est également possible de réémettre les trames avant la fin de transmission (d'où l'antériorité de l'adresse MAC de destination) à condition de renoncer au CRC (qui permet de détecter les erreurs de transmission).

### À noter : 
- Un switch n'a pas besoin d'adresse MAC 
- Le niveau 3 (IP) n'est pas utile s'il n'y a qu'un seul réseau physique
- La qualité d'un switch repose principalement sur sa capacité à vider rapidement les trames de sa mémoire
- La taille des trames (~1500 octets) a été choisie historiquement pour éviter les collisions sur les réseaux électriques
- Le rendement électrique d'ethernet est très faible (~25%)

## Les VLANs

Les VLANs permettent de réaliser une segmentation des supports de communication. Ces segments sont isolés les uns des autres, par conséquent chaque segement est situé sur un réseau différent. L'utilisation d'un routeur permet rendre la séparation perméable. 

L'avantage est de pouvoir créer des ensembles d'équipement cohérents (comme des groupes de travail) et plus sécuritaire (identification et gestion des droits, limite l'accès aux trames de broadcast). Le support de communication étant logique et non physique, le réseau n'est plus tributaire de l'emplacement physique. Au passage on fait également une économie de matériel.

Les équipements terminaux ignorent leur appartenance à un VLAN, ils continuent d'utiliser le protocole ethernet. Dans ce cas, le n° VLAN peut être associé soit à la prise du switch (statique), soit sur l'adresse MAC de l'équipement ou via une authentification (dynamique).

Généralement les switchs et le routeur sont reliés par des liens trunk (i.e. où l'étiquettage des trames est activé, par exemple 802.1q) ce qui facilite la commutation des trames pour chaque VLAN.


# Niveau 3 
## Le routage

### L'adresse IP
L'adresse IP est une adresse "logique" qui permet à la fois d'identifier le réseau (et donc le support de communication) ainsi que l'équipement. Les adresses IPv4 sont constitués de 4 octets et les adresses IPv6 de 16 octets. 

Une adresse est associée à un masque de sous-réseau qui permet de déterminer à partir de l'adresse ip l'adresse réseau. Par exemple l'adresse ip de l'équipement `178.108.223.14` avec le masque `/24 (255.255.255.0)` donne le réseau `178.108.223.0`.

Pour palier au manque d'adresse, il existe des plages d'adresses *privées* qui ne peuvent pas être utilisées sur le réseau publique (les paquets seront automatiquement rejetés) : 
- 10.x.x.x
- 172.16 à 31.x.x
- 192.168.1 à 255.x

Une adresse IP ne permet pas de connaître l'utilité d'un équipement. Dans la plupart des cas, on utilise une architecture client-serveur qui permet un partage de responsabilité. C'est au client de trouver le serveur (e.g. résolution DNS) afin de communiquer avec lui. Le serveur quant à lui doit être disponible pour répondre aux requêtes des clients, sachant que celles-ci contiennent tout ce qu'il faut pour qu'il puisse envoyer sa réponse. 

### Le protocole IP
Le protocole IP permet d'interconnecter des supports de communication entre eux et sans les confondre. Essentiellement, l'objectif est de propager des données d'un support de communication à un autre. 

Bien que le routage est similaire par certains aspects à la commutation, il ne faut absolument pas les confondre car leur utilité et leurs objectifs sont fondamentalement différents.

L'IP est un système hiérarchique composé de 3 niveaux :
- Global, régie par le découpage en 5 zones de l'IANA
- Intermédiaire, qui sont les différents réseaux
- Local, qui est un réseau

L'un des prérequis pour le fonctionnement d'IP est de garder une cohérence géographique, afin de faciliter le routage et l'agrégation de routes.

L'agrégation de routes permet d'exploiter la syntaxe IP pour désigner une plage d'adresses, avec certaines restrictions suivantes (plage continue, puissance de 2, etc.). Par exemple `192.168.0.0/24` et `192.168.1.0/24` peuvent être agrégées en `192.168.0.0/23` (en décalant le masque de sous-réseau), contrairement à `192.168.10.0/24` et `192.168.20.0/24` qui ne peuvent pas être agrégées. 

### Le routeur
Le routeur doit être connecté sur un support de communication et se comporte comme un ordinateur (contrairement au switch, un routeur a forcément au moins une adresse MAC). Le routeur retire l'entête ethernet, la détruit, traite le paquet ip pour assurer le transport du support de communication source à celui de destination, puis réencapsule les données dans une autre trame.

Les paquets ip sont traités selon une table de routage, qui contient une description exhaustive du plan ip, qui permet de forwarder n'importe quel paquet ip, qu'importe l'adresse de destination. Tous les équipements qui fabriquent des trames possèdent une table de routage.

L'administrateur réseau choisit le support de communication et l'IP du premier équipement du réseau. Ensuite les adresses peuvent être distribuées par DHCP par rapport aux adresses déjà attribuées.

### La table de routage
La table de routage permet de déterminer le prochain saut pour acheminer les paquets ip à leur destination.

#### La table d'un ordinateur
Celle-ci contient :
- Son réseau
- Tous les autres réseaux `0.0.0.0`

Si l'ip de destination se trouve sur le réseau, il peut communiquer via ethernet (même support de communication). Dans le cas contraire, il utilise la passerelle par défaut (le routeur) pour transmettre ses données.

Pour connaitre l'adresse MAC d'un équipement associé à une adresse ip, on peut utiliser ARP. Cela consiste à envoyer une requête broadcast limité au réseau local (i.e. qui ne traverse pas le routeur) et d'attendre la réponse du détenteur de l'ip cible qui répondra avec son adresse MAC.

La paserelle par défaut est désigné par son ip (et non son adresse MAC) en raison de la fiabilité de l'adresse logique (l'adresse MAC étant associé à une carte réseau, si celle-ci tombe en panne le routage ne fonctionnera plus).

#### La table du routeur
La *découverte locale* permet de construire la table de routage d'un routeur. Pour la compléter, on peut utiliser du routage statique (i.e. à la main) ou dynamique avec un protocole de routage (comme OSPF ou RIP, ou encore BGP en bordure d'opérateur) pour permettre aux routeurs proches de partager leurs table de routage. Les routeurs ne partagent que les réseaux pour lequels il a une route, et ne partage que sa meilleure route.

De base, il n'est pas possible d'imposer un point de passage si le prochain saut n'est pas un équipement voisin direct. Il n'y a aucune garantit qu'un voisin direct l'enverra au point de passage. Pour atteindre cet objectif, on peut par exemple utiliser un modèle de routage étendu.

En routage, la redondance est voulue car elle permet d'acheminer le traffic même en cas de panne ou de changement. Les routeurs transmettrent une mesure de la qualité de la connexion lors des annonces de routage (nombre de saut, débit, taille du réseau, etc.) ce qui permet d'obtenir une table plus complète et favoriser les meilleures routes. Le temps entre chaque annonce et le nombre de routes à mémoriser est un choix important pour un routeur.

Généralement le routeur ne contient qu'une seule table de routage, mais il peut en posséder plusieurs (e.g. routage récursif, VPN MPLS)...

### À noter :
- Un routeur n'a pas besoin d'ip au sens fonctionnel (son adresse MAC pourrait suffire), néanmoins cela empêcherai de réaliser la découverte locale et la résistance aux pannes seraient moins robustes.
- L'IPv6 a été établie non pas pour palier au manque d'adresse d'IPv4, mais essentiellement à cause du manque de cohérence géographique d'IPv4 (e.g. délocalisation d'entreprises, etc.). Une table de routage IPv4 de premier niveau contient environ 800 000 entrées.

## La translation (NAT)

La translation permet de connecter des réseaux privés à des réseaux publiques. Pour réaliser cette opération, les paquets transitent par un routeur capable de translation (généralement un pare-feu) qui se charge d'altérer les paquets ip pour qu'ils soient amenés à leur destination.

Ceci est réaliser grâce à l'*identification de connexion* composé des adresses ip source et de destination ainsi que des ports tcp/udp source et de destination choisi par l'OS de l'équipement.

Exemple de translation (en minuscules les adresses privées, en majuscule les adresses publiques) :

|IP source|IP destination|Port source|Port destination||
|--|--|--|--|--|
|client|SERVER|port_src|port_dest||
|ROUTEUR|SERVER|port_src*|port_dest|L'ip source est modifiée|
|SERVER|ROUTEUR|port_dest|port_src*||
|SERVER|client|port_dest|port_src|L'ip destination est modifiée|

*En cas de collision d'identifiant de connexion, le port source est altéré et le NAT garde en mémoire cette modification pour l'annuler lors de la réponse.

La translation, outre la résolution du problème d'épuisement des adresses ip permet d'offrir une certaine sécurité (les clients sont invisibles aux yeux de l'internet).

|Translation||||
|--|--|--|--|
|Dynamique|Faire croire qu'un client privé est publique|ip source d'une requête sortante|ip destination d'une réponse entrante|
|Statique|Faire croire qu'un serveur privé est publique|ip destination d'une requête entrante|ip source d'une réponse sortante
|Inverse|Faire croire qu'un client public est privé|ip source d'une requête entrante|ip destination d'une réponse sortante
|Inverse|Faire croire qu'un serveur public est privé|ip source d'une requête sortante|ip destination d'une réponse entrante

# Résume et précisions niveaux 1/2

Règles de base :
- 1 support de communication = 1 réseau
- Des supports de communication différents = Des réseaux différents
Si des équipements sont sur le même support mais sur des réseaux différents, ils ne pourront pas se voir.

La résolution ip a pour vocation d'être assez statique (le routage est essentiellement réalisé par les mêmes intermédiaires) tandis que la résolution mac peut être un peu plus dynamique (changement ou arrêt d'équipements, etc.)

||Switch|Routeur|
|--|--|--|
|Gère|Trames|Paquets|
|Fonctions|Commutation|Routage, inclusion|
||Consulte N2|Détruit N2, consulte N3, réencapsule N2|
|Table|N° de prise / Adresse MAC|Adresse IP / IP prochain saut|

C'est le switch qui sépare en différents VLANs, c'est le routeur qui autorise les communications utiles entre ces VLANs.

# Niveau 4

## La virtualisation

### Les machines virtuelles (VMs) et containers

L'OS hôte exploite le matériel physique. On parle d'hyperviseur s'il est spécialement conçu pour l'exploitation du matériel pour le bénéfice d'autres OS invités. A ne pas confondre avec des moniteurs de VM qui permet la création de VM (celui-ci étant vu par l'OS hôte comme un simple programme et donc limité par les ressources attribués par l'OS hôte pour le moniteur).

Une VM une émulation d'un ordinateur et est constitué de :
- Un ensemble de ressources physique
- Un OS invité
- Tous les programmes installés dessus
Elle se comporte exactement comme un ordinateur physique avec des caractéristiques identiques.

Un container partage les ressources attribué par OS hôte en pensant avoir l'utilisation exclusive de ces ressources (RAM, FS, adresse IP, ect.). C'est généralement utilisé pour déployer des applications pré-installés. 

L'objectif est de mutualiser la gestion de la ressource matérielle et de facilement gérer le cycle de vie du déploiement. On peut donc utiliser des VMs pour déployer des ressources puis y installer des containers.

Le partitionnement des ressources et l'isolation des VMs assurent que les VMs peuvent fonctionner en même temps sans avoir aucun impact sur les autres (e.g. ne pas consommer les ressources des autres VMs). L'isolement peut être exploité pour faire du provisionnement dynamique en fonction des ressources disponibles (e.g. le client a 200 Go dans son contrat mais n'en utilise que 100, on peut donc les donner à d'autres).

L'encapsulation permet de gérer les VMs comme des fichiers et d'effectuer des actions basiques (démarrer, arrêter, déplacer, etc.). Cela permet également de pouvoir faire des snapshots des VMs et de pouvoir retourner à un état antérieur par la suite. L'interposition garantit que la gestion des VMs ne peut être fait que par l'hyperviseur où le moniteur de VMs.

La déduplication permet d'éviter de stocker des données identiques plusieurs fois (e.g. l'OS ou certains fichiers)

On distingue 3 types de virtualisations :

||Type 2|Type 1|Type 0|
|--|--|--|--|
|Programmes/services| x | x | x |
|OS invité| x | x | 
|Hardware virtuel| x | x | |
|Container engine| | | x |
|Moniteur de VMs| x | |
|OS hôte| x | | x |
|Hyperviseur| | x | |
|Hardware| x | x | x |

Le type 2 permet la virtualisation sur une machine généraliste avec une émulation complète de hardware, mais la VM s'exécute selon les privilèges du moniteur de VMs, on est donc limité en performances.

Le type 1 est la virtualisation avec un OS hôte hyperviseur. Il existe alors 3 sous-types :
- La *paravirtualisation*, où l'OS invité est modifié pour faire des appels API à l'hyperviseur pour les instructions en mode privilégiés. L'OS invité est conscient qu'il est virtualisé. Bonnes performances.
- La *virtualisation totale*, où les instructions en mode privilégiés de l'OS invité sont translatées par l'hyperviseur qui émule complètement le microprocesseur vu par l'OS invité.
- La *virtualisation assistée par le matériel*, qui nécessite des microprocesseur spéciaux qui permettent d'excuter les instructions de l'OS invité sans les modifier.

Le type 0 n'émule pas le matériel. L'OS hôte fournit des environnement d'exécutions isolés, mais le container est restreint au même OS que l'hôte (étant donné qu'un container ne contient pas d'OS).

### Les réseaux virtuels

De base le réseau est plutôt distribué, néanmoins on peut adopter une architecture *Software Defined  Network* (SDN) pour faire du réseau partiellement centralisé pour qu'il corresponde aux attentes du client et de ceux qui utilisent le réseau. Par exemple, modifier la règle de forwarding pour que ça passe par un équipement en particulier comme un firewall que le client a pris en option ou d'autres équipement réseaux.

Il existe plusieurs façons pour virtualiser le réseau.

#### Un switch virtuel
Les VMs appartiendront au même réseau que l'hôte (même support de communication) et doivent chacune avoir une adresse MAC. Le réseau virtuel agit donc comme une extension du réseau physique.

#### Un switch virtuel + VLAN
Les VMs peuvent appartenir à des réseaux différents qui existent déjà dans le réseau physique. L'hôte utilise des trames étiquettées. Il est possible d'avoir l'hyperviseur ou l'OS hôte qui n'est pas dans le même VLAN que ses VMs. 

#### Un routeur virtuel
Les VMs appartiendront à un réseau différent qui n'existe pas encore dans l'architecture. L'hôte utilise ethernet pour communiquer avec ses VMs et route les communications entre le réseau virtuel et son réseau principal.

#### Un routeur virtuel + NAT
Les VMs appartiendront à un réseau privé qui n'est pas visible du reste de l'architecture. L'identité des VMs est cachée derrière celle de l'hôte. L'hôte utilise ethernet.

### Les datacenters

Quatre réseaux sont necessaires : 
- Le réseau de production
- Le réseau de stockage (*Storage Area Network*), pour mettre à disposition les ressources de stockage
- Le réseau de migration, pour migrer les VMs sans les interrompre
- Le réseau de gestion, pour gérer le datacenter

Lors de la mise en place d'un datacenter, les deux première choses à faire sont de mettre en place le DNS pour que les machines puissent se trouver et l'horodatage pour que les communications à certificats fonctionnent correctement et que les machines soient synchronisées.
