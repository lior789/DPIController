
from mininet.topo import Topo
from mininet.topolib import TreeTopo
from mininet.cli import CLI
from mininet.log import lg
from mininet.node import Node
from mininet.node import RemoteController
from mininet.node import OVSSwitch
from mininet.net import Mininet
from mininet.util import customConstructor, splitArgs
import argparse

class OpenFlow13Switch( OVSSwitch ):
	"Customized subclass of OVSSwitch to support openflow 13"
	def __init__( self, name, *args, **params ):
		params['protocols'] = 'OpenFlow13'
		OVSSwitch.__init__( self, name, *args, **params )


def connectSDN2DPI(net,controllerHost):
	c0 = net.get('c0')
	net.addLink(c0,controllerHost)
	controllerHost.cmd('ifconfig h1-eth1 10.100')
	c0.cmd('ifconfig c0-eth0 10.101')
	controllerHost.cmd('route add -host 10.0.0.101 dev h1-eth1')
	print 'link added between sdn-controller and dpiControllerHost'

def parseArgs():
	parser = argparse.ArgumentParser()
	parser.add_argument("dpiControllerHost",help="host of the dpi controller within the mininet")
	parser.add_argument("depth",help="the depth of the mininet tree",type=int)
	parser.add_argument("fanout",help="the fanout of the mininet tree",type=int)
	args = parser.parse_args()	
	return args

if __name__ == '__main__':	
	args = parseArgs()
	topo = TreeTopo( depth=args.depth, fanout=args.fanout )
	switch = OpenFlow13Switch
	controller = RemoteController
	net = Mininet(topo=topo,switch=switch,controller = controller)
	net.start()
	controllerHost = net.get(args.dpiControllerHost)
	connectSDN2DPI(net,controllerHost)
	CLI( net )
	net.stop()
