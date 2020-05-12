package net.mbonnin.githubgraphqlsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import net.mbonnin.githubgraphqlsample.type.CustomType
import okhttp3.OkHttpClient
import timber.log.Timber
import android.provider.Settings.System.DATE_FORMAT
import android.support.v7.widget.LinearLayoutManager
import com.apollographql.apollo.api.*
import io.reactivex.android.schedulers.AndroidSchedulers
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.api.Operation.Variables
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.fetcher.ApolloResponseFetchers.NETWORK_FIRST
import com.apollographql.apollo.fetcher.ResponseFetcher
import kotlinx.coroutines.*
import org.jetbrains.annotations.NotNull
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val scope = CoroutineScope(Dispatchers.Main + Job())

    val apollo by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(BuildConfig.GITHUB_TOKEN))
            .build()

        val dateCustomTypeAdapter = object : CustomTypeAdapter<String> {
            override fun decode(value: CustomTypeValue<*>): String {
                return value.value.toString()
            }

            override fun encode(value: String): CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(value)
            }
        }

        val cacheFactory = SqlNormalizedCacheFactory(this, "graphql_db")

        ApolloClient.builder()
            .serverUrl("https://api.github.com/graphql")
            .addCustomTypeAdapter(CustomType.DATETIME, dateCustomTypeAdapter)
            .normalizedCache(cacheFactory)
            .okHttpClient(okHttpClient)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val query = GetCommitsQuery()


        scope.launch {
            val response = try {
                val queryCall = apollo.query(query)
                    .responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                queryCall.toDeferred().await()
            } catch (e: Exception) {
                Timber.e(e)
                return@launch
            }

            populateRecyclerView(response.data)
        }
    }

    private fun populateRecyclerView(data: GetCommitsQuery.Data?) {
        val head = data
            ?.repository
            ?.ref
            ?.target as? GetCommitsQuery.AsCommit

        val commitList = head
            ?.history
            ?.nodes
            ?.filterNotNull()

        if (commitList != null) {
            recyclerView.adapter = CommitAdapter(commitList)
        } else {
            Timber.e("invalid response received")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
