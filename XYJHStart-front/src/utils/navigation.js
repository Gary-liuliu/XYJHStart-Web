let appRouter = null

export function setAppRouter(router) {
  appRouter = router
}

export function goToLogin() {
  if (appRouter) {
    appRouter.push({ name: 'Login' })
    return
  }

  if (window.location.pathname !== '/') {
    window.location.replace('/')
  }
}
