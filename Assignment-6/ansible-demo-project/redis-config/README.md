# Redis Ansible Role

Ansible role for installing and configuring Redis on Ubuntu.

## Requirements

- Ubuntu EC2 instance
- Ansible
- SSH access to the EC2 instance

## Role Variables

- `redis_package`: Redis package name
- `redis_service`: Redis service name
- `redis_port`: Redis port
- `redis_bind`: Redis bind address
- `redis_config_file`: Redis configuration file

## Usage

```yaml
- hosts: redis
  become: true
  roles:
    - redis-config