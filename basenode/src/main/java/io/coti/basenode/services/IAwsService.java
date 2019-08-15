package io.coti.basenode.services;

import java.io.IOException;

public interface IAwsService {

    void downloadClusterStampFile(String fileName) throws IOException;
}
