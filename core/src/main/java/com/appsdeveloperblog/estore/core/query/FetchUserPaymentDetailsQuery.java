package com.appsdeveloperblog.estore.core.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchUserPaymentDetailsQuery {
    private String userId;
}
