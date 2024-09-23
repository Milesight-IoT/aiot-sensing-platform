import type { Emitter } from 'mitt';
import mitt from 'mitt';

type Events = {
    resize: {
        detail: {
            width: number;
            height: number;
        };
    };
    openPanel: string;
    tagViewsChange: string;
    tagViewsShowModel: string;
    logoChange: boolean;
    changLayoutRoute: {
        indexPath: string;
        parentPath: string;
    };
    showPanel: boolean;
    panelTitle: String;
    updateBread: any;
};

export const emitter: Emitter<Events> = mitt<Events>();
