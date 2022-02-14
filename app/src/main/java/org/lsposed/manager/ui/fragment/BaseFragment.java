/*
 * <!--This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2021 LSPosed Contributors-->
 */

package org.lsposed.manager.ui.fragment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.lsposed.manager.App;
import org.lsposed.manager.R;
import org.lsposed.manager.databinding.ActivityMainBinding;
import org.lsposed.manager.ui.activity.MainActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class BaseFragment extends Fragment {
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    protected ActivityMainBinding activityMainBinding;
    protected @DrawableRes int showFab = 0;

    public void navigateUp() {
        getNavController().navigateUp();
    }

    public NavController getNavController() {
        return NavHostFragment.findNavController(this);
    }

    public boolean safeNavigate(@IdRes int resId) {
        try {
            getNavController().navigate(resId);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public boolean safeNavigate(NavDirections direction) {
        try {
            getNavController().navigate(direction);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        var activity = requireActivity();
        if (activity instanceof MainActivity) {
            activityMainBinding = ((MainActivity) context).getActivityBinding();
        }
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        if (showFab != 0) {
            activityMainBinding.fab.setImageResource(showFab);
            activityMainBinding.fab.show();
        }
        else activityMainBinding.fab.hide();
        super.onResume();
    }

    @Override
    public void onDetach() {
        activityMainBinding = null;
        super.onDetach();
    }

    public void setupToolbar(int title) {
        setupToolbar(getString(title), -1);
    }

    public void setupToolbar(int title, int menu) {
        setupToolbar(getString(title), menu, null);
    }

    public void setupToolbar(String title, int menu) {
        setupToolbar(title, menu, null);
    }

    public void setupToolbar(String title, int menu, View.OnClickListener navigationOnClickListener) {
        if (activityMainBinding == null) {
            var activity = requireActivity();
            if (activity instanceof MainActivity) {
                activityMainBinding = ((MainActivity) activity).getActivityBinding();
            }
        }
        var toolbar = activityMainBinding.toolbar;
        var toolbarLayout = activityMainBinding.toolbarLayout;
        var clickView = activityMainBinding.clickView;
        toolbar.setNavigationOnClickListener(navigationOnClickListener == null ? (v -> navigateUp()) : navigationOnClickListener);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbarLayout.setTitle(title);
        toolbarLayout.setTooltipText(title);
        clickView.setTooltipText(title);
        toolbar.getMenu().clear();
        if (menu != -1) {
            toolbar.inflateMenu(menu);
            toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
            onPrepareOptionsMenu(toolbar.getMenu());
        }
    }

    protected void showFabAndBottomNav() {
        var nav = activityMainBinding != null ? activityMainBinding.nav : null;
        if (nav instanceof BottomNavigationView) {
            var behavior = ((CoordinatorLayout.LayoutParams) nav.getLayoutParams()).getBehavior();
            if (behavior instanceof HideBottomViewOnScrollBehavior) {
                //noinspection unchecked
                ((HideBottomViewOnScrollBehavior<BottomNavigationView>) behavior).slideUp((BottomNavigationView) nav);
            }
        }
        var fab = activityMainBinding != null ? activityMainBinding.fab : null;
        if (fab != null) {
            var behavior = ((CoordinatorLayout.LayoutParams) nav.getLayoutParams()).getBehavior();
            if (behavior instanceof HideBottomViewOnScrollBehavior) {
                //noinspection unchecked
                ((HideBottomViewOnScrollBehavior<FloatingActionButton>) behavior).slideUp(fab);
            }
        }
    }

    public void runAsync(Runnable runnable) {
        App.getExecutorService().submit(runnable);
    }

    public <T> Future<T> runAsync(Callable<T> callable) {
        return App.getExecutorService().submit(callable);
    }

    public void runOnUiThread(Runnable runnable) {
        uiHandler.post(runnable);
    }

    public <T> Future<T> runOnUiThread(Callable<T> callable) {
        var task = new FutureTask<>(callable);
        runOnUiThread(task);
        return task;
    }

    public void showHint(@StringRes int res, boolean lengthShort, @StringRes int actionRes, View.OnClickListener action) {
        showHint(App.getInstance().getString(res), lengthShort, App.getInstance().getString(actionRes), action);
    }

    public void showHint(@StringRes int res, boolean lengthShort) {
        showHint(App.getInstance().getString(res), lengthShort, null, null);
    }

    public void showHint(CharSequence str, boolean lengthShort) {
        showHint(str, lengthShort, null, null);
    }

    public void showHint(CharSequence str, boolean lengthShort, CharSequence actionStr, View.OnClickListener action) {
        if (isResumed()) {
            var container = requireActivity().findViewById(R.id.container);
            if (container != null) {
                var snackbar = Snackbar.make(container, str, lengthShort ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG);
                if (container.findViewById(R.id.nav) instanceof BottomNavigationView)
                    snackbar.setAnchorView(R.id.nav);
                if (container.findViewById(R.id.fab) instanceof FloatingActionButton)
                    snackbar.setAnchorView(R.id.fab);
                if (actionStr != null && action != null) snackbar.setAction(actionStr, action);
                snackbar.show();
                return;
            }
        }
        runOnUiThread(() -> {
            try {
                Toast.makeText(App.getInstance(), str, lengthShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            } catch (Throwable ignored) {
            }
        });
    }

}
