#!/usr/bin/bash
SOLO_CLUSTER_NAME=solo
SOLO_DEPLOYMENT=solo-deployment

solo relay node destroy -i node1 --deployment "${SOLO_DEPLOYMENT}" --cluster-ref kind-${SOLO_CLUSTER_NAME}
solo explorer node destroy --deployment "${SOLO_DEPLOYMENT}" --force
solo mirror node destroy --deployment "${SOLO_DEPLOYMENT}" --force
solo consensus network destroy --deployment "${SOLO_DEPLOYMENT}" --force

kind get clusters | grep '^solo' | while read cluster; do
  kind delete cluster -n "$cluster"
done

rm -rf ~/.solo

docker volume prune

