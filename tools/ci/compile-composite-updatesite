#!/usr/bin/env python3

import os
from time import time
from xml.dom import minidom
import xml.etree.ElementTree as ET
import argparse

TEMPLATE = """<repository>
                <properties/>
                <children/>
              </repository>"""

TARGETS = [
    {'output': 'compositeArtifacts.xml', 'pi_name': 'compositeArtifactRepository',
     'type': "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository"},
    {'output': 'compositeContent.xml', 'pi_name': 'compositeMetadataRepository',
     'type': "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository"},
]

parser = argparse.ArgumentParser(description='Merges multiple Eclipse updatesites')
parser.add_argument('name', help='the name of the merged updatesite')
parser.add_argument('version', help='the version of the merged updatesite')
args = parser.parse_args()


def generate(name, type, version):
    repo = ET.fromstring(TEMPLATE)

    repo.attrib["type"] = type
    repo.attrib['name'] = name
    repo.attrib['version'] = version

    properties = repo.find('properties')
    properties.attrib['size'] = "1"
    properties.append(ET.Element('property', {
        'name': 'p2.timestamp',
        'value': str(int(time())),
    }))

    directories = [file for file in os.listdir() if os.path.isdir(file)]
    if directories:
        children = repo.find('children')
        children.attrib['size'] = str(len(directories))
        for entry in directories:
            children.append(ET.Element('child', {'location': entry}))

    return ET.tostring(repo, encoding='unicode', method="xml")


def add_processing_instruction(pi_name, xml_string):
    dom = minidom.parseString(xml_string)
    pi = dom.createProcessingInstruction(pi_name, "version='1.0.0'")
    dom.insertBefore(pi, dom.documentElement)
    return dom


def prettify(dom):
    almost_pretty = dom.toprettyxml(indent='  ', encoding='utf-8')
    almost_pretty = almost_pretty.decode('utf-8')
    return '\n'.join([l for l in almost_pretty.split('\n') if l.strip()])


def create(descriptor):
    with open(descriptor['output'], 'w') as file:
        dom = add_processing_instruction(descriptor['pi_name'], generate(args.name, descriptor['type'], args.version))
        file.write(prettify(dom) + "\n")


if __name__ == '__main__':
    for target in TARGETS:
        create(target)
