delimiter ;

call DBCreateNewUserHashed('paki', 'Paki', 'pakipaki', '', '', '', ''); -- UID=1
call DBCreateNewUserHashed('poki', 'Poki', 'pokipoki', '', '', '', ''); -- UID=2
call DBCreateNewUserHashed('pokipa', 'Pokipa', 'pokipoki', '', '', '', ''); -- UID=3

call createNewPermintaan(1, 'KejuBakar', 'Deskripsi', '2017-01-01'); -- PID=1
call createNewPermintaan(1, 'KejuGulung', 'Deskripsi', '2017-01-01'); -- PID=2
call createNewPenawaran(2, 'Mahyong', 'Deskripsi', 60000); -- PID=3

call getPostDetail(1);

call createThread(1, 2, 'Mau gw kasih?');
call createThread(1, 2, 'Mau gw kasih?');
call createThread(1, 3, 'Baa baa black sheep');

call replyThread(1, 1, 2, 'Wiiii');

call getThreads(1, 1);
call getThreads(1, 2);
call getThreads(1, 3);

call initiateTransferPeminjaman(1, 2, '2017-01-01');
call initiateTransferPeminjaman(1, 3, '2017-01-01');

call confirmTransferPeminjaman(1, 2);

call createThread(3, 1, 'Baaaa~');
call getThreads(3, 2);

call initiateTransferPenawaran(3, 1, '2017-01-01');
call confirmTransferPenawaran(3);
