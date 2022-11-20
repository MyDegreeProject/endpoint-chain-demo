import request from '@/utils/request'

export function getList(data) {
  return request({
    url: '/endpointChain/endpoint/list',
    method: 'post',
    data
  })
}

export function getEndpointList(data) {
  return request({
    url: '/endpointChain/endpoint/list',
    method: 'post',
    data
  })
}

export function getChainInfo() {
  return request({
    url: '/endpointChain/user/chainInfo',
    method: 'post'
  })
}

export function deployContract(data) {
  return request({
    url: '/endpointChain/user/deployContract',
    method: 'post',
    data
  })
}

export function addEndpoint(data) {
  return request({
    url: '/endpointChain/endpoint/add',
    method: 'post',
    data
  })
}

export function modifyEndpoint(data) {
  return request({
    url: '/endpointChain/endpoint/modify',
    method: 'post',
    data
  })
}

export function generatorSummaries(data) {
  return request({
    url: '/endpointChain/endpoint/generatorSummaries',
    method: 'post',
    data
  })
}

export function getEndpointSummaries() {
  return request({
    url: '/endpointChain/endpoint/generatorSummaries',
    method: 'post'
  })
}

export function getSparqlSelectResult() {
  return request({
    url: '/endpointChain/endpoint/sparql',
    method: 'post'
  })
}

export function getEndpointSelectList() {
  return request({
    url: '/endpointChain/endpoint/selectList',
    method: 'post'
  })
}

export function getEndpointSummaryDataList(data) {
  return request({
    url: '/endpointChain/data/list',
    method: 'post',
    data
  })
}

export function validChain(data) {
  return request({
    url: '/endpointChain/data/validChain',
    method: 'post',
    data
  })
}

export function previewChain(data) {
  return request({
    url: '/endpointChain/data/previewChain',
    method: 'post',
    data
  })
}

export function getTotalInfo() {
  return request({
    url: '/endpointChain/data/getTotalInfo',
    method: 'post'
  })
}

export function getUser(userId) {
  return request({
    url: '/endpointChain/user/' + userId,
    method: 'post'
  })
}
