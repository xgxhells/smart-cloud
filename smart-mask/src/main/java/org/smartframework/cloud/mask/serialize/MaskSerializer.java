package org.smartframework.cloud.mask.serialize;

import java.lang.reflect.Field;

import org.smartframework.cloud.mask.DefaultMaskConfig;
import org.smartframework.cloud.mask.MaskLog;
import org.smartframework.cloud.mask.MaskRule;
import org.smartframework.cloud.mask.util.MaskUtil;

import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * @desc mask序列化器
 * @author liyulin
 * @date 2019/11/06
 */
@Slf4j
public class MaskSerializer extends JavaBeanSerializer {

	public MaskSerializer(Class<?> beanType) {
		super(beanType);
	}

	@Override
	protected Object processValue(JSONSerializer jsonBeanDeser, BeanContext beanContext, Object object, String key,
			Object propertyValue) {
		try {
			Field field = beanContext.getField();
			if (MaskSerializeHelper.isNeedMask(field)) {
				MaskLog maskLog = field.getAnnotation(MaskLog.class);
				int startLen, endLen;
				String mask;
				if (isSetMaskAttributes(maskLog)) {
					startLen = maskLog.startLen();
					endLen = maskLog.endLen();
					mask = maskLog.mask();
				} else {
					MaskRule maskRule = maskLog.value();
					startLen = maskRule.getStartLen();
					endLen = maskRule.getEndLen();
					mask = maskRule.getMask();
				}
				return MaskUtil.mask(propertyValue, startLen, endLen, mask);
			}
		} catch (SecurityException | IllegalArgumentException e) {
			log.error("process value error by reflection", e);
		}

		return super.processValue(jsonBeanDeser, beanContext, object, key, propertyValue);
	}

	/**
	 * 是否已设置{@link MaskLog}的非规则属性（除了value以外的）
	 * 
	 * @param maskLog
	 * @return
	 */
	private boolean isSetMaskAttributes(MaskLog maskLog) {
		return maskLog.startLen() != DefaultMaskConfig.START_LEN || maskLog.endLen() != DefaultMaskConfig.END_LEN
				|| !DefaultMaskConfig.MASK_TEXT.equals(maskLog.mask());
	}

}