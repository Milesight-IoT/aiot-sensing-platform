// 完整版菜单比较多，将 rank 抽离出来，在此方便维护

const home = 0, // 平台规定只有 home 路由的 rank 才能为 0 ，所以后端在返回 rank 的时候需要从 1 开始哦
    dashboard = 1,
    device = 2,
    object = 3,
    otaUpdate = 4,
    rules = 5,
    system = 6;

export { home, dashboard, device, object, otaUpdate, rules, system };
