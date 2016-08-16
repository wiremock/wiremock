package com.github.tomakehurst.wiremock.admin;

import java.util.List;

public interface Paginator<T> {

    List<T> select();
    int getTotal();
}
