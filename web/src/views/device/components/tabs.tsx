import Details from "./tabs/details/index.vue";
import RowDrag from "./tabs/telemetry/index.vue";
import Ota from "./tabs/ota.vue"
export const tabs = [
    {
        key: "details",
        content: "common.details",
        title: "common.details",
        component: Details
    },
    {
        key: "rowDrag",
        content: "device.latestTelemetry",
        title: "device.latestTelemetry",
        component: RowDrag
    },
    {
        key: "rowDrag",
        content: "ota.update",
        title: "ota.update",
        component: Ota
    }
];
