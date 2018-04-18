package com.deitel.recycleviewtest.base;


import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public abstract class StringPresenter<T> implements PVContract.Presenter {

    private final PVContract.View<T> view;

    private final CompositeDisposable mCompositeDisposable;

    protected StringPresenter(PVContract.View<T> view) {
        this.view = view;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void unsubscribe() {
        mCompositeDisposable.clear();
    }

    @Override
    public void get(String url) {
        url = dealUrl(url);
        mCompositeDisposable.add(
                OkGo.<String>get(url)
                        .converter(new StringConvert())
                        .adapt(new ObservableResponse<String>())
                        .subscribeOn(Schedulers.io())
                        .map(new Function<Response<String>, T>() {
                            @Override
                            public T apply(@NonNull Response<String> s) throws Exception {
                                return dealResponse(s.body());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<T>() {
                            @Override
                            public void accept(@NonNull T result) throws Exception {
                                view.success(result);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                view.error(throwable.getMessage());
                            }
                        })
        );
    }
    protected String dealUrl(String url) {
        return url;
    }

    protected abstract T dealResponse(String s);
}
