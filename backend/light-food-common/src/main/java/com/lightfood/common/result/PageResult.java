package com.lightfood.common.result;

import java.util.List;

/**
 * Paged response payload.
 */
public record PageResult<T>(long total, List<T> records) {
}
