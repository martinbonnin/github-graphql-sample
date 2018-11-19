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
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.response.CustomTypeValue
import com.apollographql.apollo.response.CustomTypeAdapter
import io.reactivex.android.schedulers.AndroidSchedulers


class MainActivity : AppCompatActivity() {

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

        ApolloClient.builder()
            .serverUrl("https://api.github.com/graphql")
            .addCustomTypeAdapter(CustomType.DATETIME, dateCustomTypeAdapter)
            .okHttpClient(okHttpClient)
            .build()
    }

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val query = GetCommitsQuery()
        disposable = Rx2Apollo.from(apollo.query(query))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
            populateRecyclerView(it.data())
        }, {
            Timber.e(it)
        })
    }

    private fun populateRecyclerView(data: GetCommitsQuery.Data?) {
        val head = data
            ?.repository
            ?.ref()
            ?.target() as? GetCommitsQuery.AsCommit

        val commitList = head
            ?.history()
            ?.edges()

        if (commitList != null) {
            recyclerView.adapter = CommitAdapter(commitList)
        } else {
            Timber.e("invalid response received")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
