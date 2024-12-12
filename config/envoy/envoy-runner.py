import argparse
import glob
import os
import re
from pathlib import Path

BASE_DIR_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), '../..'))
THIS_FILE_DIR_PATH = os.path.abspath(os.path.dirname(__file__))


def find_proto_services(proto_path):
    services = []
    for proto_file in proto_path.rglob('*.proto'):
        with open(proto_file, 'r') as pf:
            content = pf.read()
            package_match = re.search(r'^package\s+([\w\.]+);', content, re.MULTILINE)
            service_matches = re.findall(r'^service\s+(\w+)\s+{', content, re.MULTILINE)
            if package_match and service_matches:
                package_name = package_match.group(1)
                for service_match in service_matches:
                    services.append(f"{package_name}.{service_match}")
    return services


def update_yaml_config(config_path, services):
    with open(config_path, 'r') as yf:
        lines = yf.readlines()

    services_str = f'services: {str(services)}'
    in_http_filters_block = False
    with open(config_path, 'w') as yf:
        for line in lines:
            if line.strip() == 'http_filters:':
                in_http_filters_block = True
            if in_http_filters_block and 'services:' in line:
                line = re.sub(r'services: \[.*?]', services_str, line)
            yf.write(line)

            if in_http_filters_block and line.strip() == '- name: envoy.filters.http.router':
                in_http_filters_block = False


def generate_proto_bin(proto_dir):
    v1_path = Path(proto_dir) / "provides"
    proto_directories = [x[0] + "/*.proto" for x in os.walk(v1_path) if glob.glob(x[0] + "/*.proto")]

    protoc_command = [
                         "protoc",
                         "--include_imports",
                         "--include_source_info",
                         f"--proto_path={proto_dir}",
                         f"--descriptor_set_out={THIS_FILE_DIR_PATH}/generated-bin.pb"
                     ] + proto_directories

    os.system(" ".join(protoc_command))


def run_docker_compose(docker_compose_file_path, command):
    os.system(f'docker-compose -f {docker_compose_file_path} {command}')

def find_docker_compose_file_name():
    uname = os.uname()
    if uname.sysname == 'Darwin':
        return 'docker-compose-macos.yml'
    return 'docker-compose.yml'

def find_config_file_name():
    uname = os.uname()
    if uname.sysname == 'Darwin':
        return 'config-macos.yaml'
    return 'config.yaml'

def main(args, proto_dir_http, yaml_config_file, proto_dir):
    # Define the path to your docker-compose file here
    docker_compose_file_path = THIS_FILE_DIR_PATH + "/" + find_docker_compose_file_name()

    if args.build or args.run:
        proto_path = Path(proto_dir_http)
        services = find_proto_services(proto_path)
        print('----------------------   Identified services   ----------------------')
        for service in services:
            print(service)
        update_yaml_config(yaml_config_file, services)
        generate_proto_bin(proto_dir)

    if args.stop or args.run:
        run_docker_compose(docker_compose_file_path, 'down')

    if args.start or args.run:
        run_docker_compose(docker_compose_file_path, 'up --build -d')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="A script to manage envoy.")
    parser.add_argument('--start', action='store_true', help='Start the envoy')
    parser.add_argument('--stop', action='store_true', help='Stop the envoy')
    parser.add_argument('--build', action='store_true', help='Build envoy config')
    parser.add_argument('--config', type=str, help='Specify the config file path')
    parser.add_argument('--port', type=int, help='Specify the port number')
    parser.add_argument('--proto_dir', type=str, help='Specify the proto directory path')
    parser.add_argument('--run', action='store_true', help='Build envoy config and start the envoy')
    args = parser.parse_args()

    if vars(args) == vars(parser.parse_args([])):
        args.run = True

    PROTO_DIR = BASE_DIR_PATH + "/src/main/proto/"
    if 'PROTO_DIR' in os.environ:
        PROTO_DIR = os.environ['PROTO_DIR']
    if args.proto_dir:
        PROTO_DIR = args.proto_dir

    PROTO_DIR_HTTP = PROTO_DIR + "provides/http/"
    YAML_CONFIG_FILE = THIS_FILE_DIR_PATH + "/" + find_config_file_name()
    if args.config:
        YAML_CONFIG_FILE = args.config

    main(args, PROTO_DIR_HTTP, YAML_CONFIG_FILE, PROTO_DIR)
