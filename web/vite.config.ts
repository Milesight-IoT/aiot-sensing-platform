/*
 * @Author: lin@milesight.com
 * @Date: 2023-01-13 13:43:06
 * @LastEditTime: 2024-09-11 16:30:06
 * @Descripttion: Sensing Platform
 */
import dayjs from 'dayjs';
import {resolve} from 'path';
import pkg from './package.json';
import {warpperEnv, regExps} from './build';
import {getPluginsList} from './build/plugins';
import {include, exclude} from './build/optimize';
import {UserConfigExport, ConfigEnv, loadEnv} from 'vite';

/** 当前执行node命令时文件夹的地址（工作目录） */
const root: string = process.cwd();

/** 路径查找 */
const pathResolve = (dir: string): string => {
    return resolve(__dirname, '.', dir);
};

/** 设置别名 */
const alias: Record<string, string> = {
    '@': pathResolve('src'),
    '@build': pathResolve('build'),
};

const {dependencies, devDependencies, name, version} = pkg;
const __APP_INFO__ = {
    pkg: {dependencies, devDependencies, name, version},
    lastBuildTime: dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'),
};

export default ({command, mode}: ConfigEnv): UserConfigExport => {
    const {VITE_CDN, VITE_PORT, VITE_COMPRESSION, VITE_PUBLIC_PATH, VITE_PROXY_DOMAIN, VITE_PROXY_DOMAIN_TAR} =
        warpperEnv(loadEnv(mode, root));
    return {
        base: VITE_PUBLIC_PATH,
        root,
        resolve: {
            alias,
        },
        // 服务端渲染
        server: {
            // 是否开启 https
            https: false,
            // 端口号
            port: VITE_PORT,
            host: '0.0.0.0',
            // 本地跨域代理 https://cn.vitejs.dev/config/server-options.html#server-proxy
            proxy:
                VITE_PROXY_DOMAIN_TAR.length > 0
                    ? {
                        '/api/ws': {
                            target: 'ws://192.168.60.79:5220',
                            rewrite: (path) => path.replace(/^\/api\/ws /, '/api/ws'),
                            changeOrigin: true,
                            ws: true,
                        },
                        [VITE_PROXY_DOMAIN]: {
                            target: VITE_PROXY_DOMAIN_TAR,
                            // ws: true,
                            changeOrigin: true,
                            rewrite: (path: string) => regExps(path, VITE_PROXY_DOMAIN),
                        },
                    }
                    : null,
        },
        plugins: getPluginsList(command, VITE_CDN, VITE_COMPRESSION),
        // https://cn.vitejs.dev/config/dep-optimization-options.html#dep-optimization-options
        optimizeDeps: {
            include,
            exclude,
        },
        build: {
            sourcemap: false,
            // 消除打包大小超过500kb警告
            chunkSizeWarningLimit: 4000,
            // 打包后静态资源存放的文件夹
            outDir: "../application/src/main/resources/static",
            emptyOutDir: true,
            rollupOptions: {
                input: {
                    index: pathResolve('index.html'),
                },
                // 静态资源分类打包
                output: {
                    chunkFileNames: 'static/js/[name]-[hash].js',
                    entryFileNames: 'static/js/[name]-[hash].js',
                    assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
                    manualChunks(id) {
                        if (id.includes('node_modules')) {
                            return id.toString().split('node_modules/')[1].split('/')[0].toString();
                        }
                    }
                },
            },
        },
        define: {
            __INTLIFY_PROD_DEVTOOLS__: false,
            __APP_INFO__: JSON.stringify(__APP_INFO__),
        },
    };
};
