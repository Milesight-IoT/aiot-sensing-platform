const renderVal = ({ index, row }) => {
    if (row.ability === 'image') return '-';
    else if (row.abilityType == 1) {
        return row.value;
    } else {
        const attrStr = row.value;
        try {
            const attrObj = JSON.parse(attrStr);
            const attrArray = Object.keys(attrObj);
            let isRed = false;
            const textObj = {};
            const textAttr = attrArray.map(item => {
                const keyName = item;
                const keyVal = attrObj[item];
                if (!keyVal) {
                    isRed = true;
                    //return (<span class="color-error">{`"${keyName}":""`}</span>)
                }
                textObj[keyName] = keyVal;
                return `"${keyName}":${keyVal}`;
            });
            row.textJson = JSON.stringify(textObj);
            if (isRed) {
                return <div class="truncate color-error">{`{${textAttr.join(',')}}`}</div>;
            } else {
                return `{${textAttr.join(',')}}`;
            }
        } catch (e) {
            const roiAttrs = attrStr.substring(1, attrStr.length - 1).split(',');
            const objectPrefix = '{';
            const objectSuffix = '}';
            row.attrObj = {};
            return (
                <div class="flex flex-wrap">
                    <span>{objectPrefix}</span>
                    {roiAttrs.length
                        ? roiAttrs.map((item, index) => {
                              const keyValArr = item.split('=');
                              const keyName = keyValArr[0]?.trim();
                              const keyValue = `${keyValArr[1]}`;
                              const className = keyValArr[1] ? '' : 'color-error';
                              row.attrObj[keyName] = keyValue;
                              return (
                                  <span class={className}>
                                      "{keyName}":"{keyValue}"{index == roiAttrs.length - 1 ? '' : ','}
                                  </span>
                              );
                          })
                        : '-'}
                    <span>{objectSuffix}</span>
                </div>
            );
        }
    }
};
export { renderVal };
