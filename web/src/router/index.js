import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

/* Layout */
import Layout from "@/layout";

export const constantRoutes = [
  {
    path: "/",
    component: () => import("@/views/index/index"),
    hidden: true
  },
  {
    path: "/login",
    component: () => import("@/views/login/index"),
    hidden: true
  },
  {
    path: "/regist",
    component: () => import("@/views/regist/index"),
    hidden: true,
  },
  {
    path: "/setting",
    component: () => import("@/views/regist/index"),
  },
  {
    path: "/404",
    component: () => import("@/views/404"),
    hidden: true
  },
  {
    path: "/endpoint",
    component: Layout,
    name: "endpoint",
    redirect:"/endpoint/list",
    meta: { title: "联邦查询"},
    children: [
      {
        path: "/endpoint/list",
        name: "endpointManage",
        component: () => import("@/views/endpoint/index"),
        meta: { title: "端点管理", icon: "endpoint" }
      },{
        path: "/endpoint/sparql",
        name: "sparqlQuery",
        component: () => import("@/views/endpoint/query"),
        meta: { title: "查询", icon: "endpoint" }
      }
    ]
  },
  {
    path: "/data",
    component: Layout,
    children: [
      {
        path: "list",
        name: "data",
        component: () => import("@/views/data/index"),
        meta: { title: "存证管理", icon: "data" }
      }
    ]
  },
  {
    path: "/user",
    component: Layout,
    children: [
      {
        path: "index",
        name: "pay",
        component: () => import("@/views/user/index"),
        meta: { title: "个人中心", icon: "user" }
      }
    ]
  },

  // 404 page must be placed at the end !!!
  { path: "*", redirect: "/404", hidden: true }
];

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes
  });

const router = createRouter();

export function resetRouter() {
  const newRouter = createRouter();
  router.matcher = newRouter.matcher; // reset router
}

export default router;
