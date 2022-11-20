import request from "@/utils/request";

export function login(data) {
  return request({
    url: "/endpointChain/user/login",
    method: "post",
    data
  });
}

export function register(data) {
  return request({
    url: "/endpointChain/user/regist",
    method: "post",
    data
  });
}

export function logout(data) {
  return request({
    url: "/endpointChain/user/logout",
    method: "post",
    data
  });
}

export function getInfo(data) {
  return request({
    url: "/endpointChain/user/{userId}",
    method: "post",
    data
  });
}